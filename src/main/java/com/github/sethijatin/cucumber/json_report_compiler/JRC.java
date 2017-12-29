package com.github.sethijatin.cucumber.json_report_compiler;

import org.apache.maven.plugin.AbstractMojo;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.*;
import java.util.*;

@Mojo(name = "my-goal", threadSafe = true, defaultPhase = LifecyclePhase.POST_INTEGRATION_TEST)
public class JRC extends AbstractMojo {

    @Parameter(readonly = true)
    private String readJsonReportsFromFolder;

    @Parameter(readonly = true)
    private String writeCompiledReportsToFolder;

    private HashMap<String, HashMap<String,String>> featureMap = new HashMap<String, HashMap<String, String>>();
    private HashMap<String, List<String>> elementMap = new HashMap<String, List<String>>();

    private void generateFeatureMapForNode (JsonNode node){

        if (!featureMap.containsKey(node.get("id").toString())) {
            HashMap<String, String> feature = new HashMap<String, String>();
            feature.put("line", node.get("line").toString());
            feature.put("name", node.get("name").toString());
            feature.put("id", node.get("id").toString());
            feature.put("description", node.get("description").toString());
            feature.put("keyword", node.get("keyword").toString());
            feature.put("uri", node.get("uri").toString());
            feature.put("tags", node.get("tags").toString());
            featureMap.put(node.get("id").toString(), feature);
        }
    }

    private void generateElementMapForNode (JsonNode node){

        JsonNode elements = node.get("elements");
        for (JsonNode element : elements){
            if (!elementMap.containsKey(node.get("id").toString())){
                List<String> elementList = new ArrayList<String>();
                elementList.add(element.toString());
                elementMap.put(node.get("id").toString(), elementList);
            }
            else {
                System.out.println(element.get("type").toString().equals("\"scenario\""));
                if (element.get("type").toString().equals("\"scenario\"")){
                    elementMap.get(node.get("id").toString()).add(element.toString());
                }
            }
        }
    }

    private String writeElements (List<String> elements){
        String elementStringStart = "";
        String elementStringStop = " ]";
        for (String element : elements){
            if (elementStringStart.equals("")){
                elementStringStart = "\"elements\" : [ " + element;
            }
            else {
                elementStringStart = elementStringStart + "," + element;
            }
        }
        return elementStringStart + elementStringStop;
    }


    private String writeFeature (String id, HashMap<String, String> feature){
        String featureStart = "{ ";
        String featureStop = " }";
        String elementList = writeElements(elementMap.get(id));

        for (Map.Entry e : feature.entrySet()){
            featureStart = featureStart + "\"" + e.getKey() + "\"" + " : " + e.getValue() + ",";
        }
        featureStart = featureStart + elementList;
        return featureStart + featureStop;
    }

    private String readFile (File file) throws Exception{
        BufferedReader br = new BufferedReader(new FileReader(file.getAbsoluteFile()));
        String fileContent = "";
        String stringContent;
        while ((stringContent = br.readLine()) != null){
            fileContent = fileContent + stringContent;
        }
        br.close();
        return fileContent;
    }

    private List<String> readJsonReport(String folderPath) throws Exception {
        List<String> jsonReports = new ArrayList<String>();
        File dir = new File(folderPath);
        File[] directoryListing = dir.listFiles();
        if (directoryListing != null) {
            for (File child : directoryListing) {
                jsonReports.add(readFile(child));
            }
        }
        return jsonReports;
    }

    private String compileJsonFile(){

        String compiledJsonStart = "";
        String compiledJsonEnd = " ]";
        for (Map.Entry feature : featureMap.entrySet()){
            String id = (String)feature.getKey();
            HashMap<String, String> valueSet = ((HashMap<String, String>) feature.getValue());
            if (compiledJsonStart.equals("")){
                compiledJsonStart = "[ " + writeFeature(id, valueSet);
            }
            else{
                compiledJsonStart = compiledJsonStart + "," + writeFeature(id, valueSet);
            }
        }
        return compiledJsonStart + compiledJsonEnd;
    }

    private void writeCompiledReport(String folderPath) throws Exception{
        String report = compileJsonFile();
        BufferedWriter bw = null;
        FileWriter fw = null;
        fw = new FileWriter(  folderPath + "/compiled-feature-report.json");
        bw = new BufferedWriter(fw);
        bw.write(report);
        bw.close();
        fw.close();
    }

    public void execute() throws MojoExecutionException, MojoFailureException {

        try {
            List<String> jsonContentList = readJsonReport(readJsonReportsFromFolder);
            ObjectMapper objMapper = new ObjectMapper();

            for (String jsonContent : jsonContentList) {
                JsonNode nodes = objMapper.readTree(jsonContent);
                for (JsonNode node : nodes) {
                    generateFeatureMapForNode(node);
                    generateElementMapForNode(node);
                }
            }
            writeCompiledReport(writeCompiledReportsToFolder);
        }
        catch (Exception e){
            System.out.println(e.getStackTrace());
        }
    }
}
