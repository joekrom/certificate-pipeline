package de.axxepta.metaselect;

import de.axxepta.converterservices.proc.Pipeline;
import de.axxepta.converterservices.tools.Saxon;
import de.axxepta.converterservices.utils.IOUtils;
import de.axxepta.converterservices.tools.Saxon;
import org.apache.commons.io.FileUtils;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.transform.*;
import javax.xml.transform.stream.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.servlet.http.HttpServletResponse;

import de.axxepta.converterservices.servlet.ServletUtils;
import de.axxepta.converterservices.utils.IOUtils;
import spark.Request;
import spark.Response;


import static de.axxepta.metaselect.Configuration.SRC_PATH;
import static de.axxepta.metaselect.Configuration.XSL_FILE_PATH;


// import net.sf.saxon.s9api.Processor;
// import net.sf.saxon.s9api.SaxonApiException;
// import net.sf.saxon.s9api.XsltExecutable;
// import net.sf.saxon.s9api.XsltTransformer;


public class xsltTransformation {

    xsltTransformation() {}

    private static final String SRC_PATH = "src/main/resources/";
    private static final String XSL_FILE_PATH = "src/main/resources/xsl/";


    // private String XSLFILE;
    // private String INFILE; 
    // private String OUTFILE; 

    // private StreamSource xslcode;
    // private StreamSource input;
    // private StreamResult output;

    // private void streaming(String pathXslCode, String pathInput, String pathOutput){
    //     this.xslcode = new StreamSource(new File(pathXslCode));
    //     this.input = new StreamSource(new File(pathInput));
    //     this.output = new StreamSource(new File(pathOutput));
    // }


    public void transform (String xslFile, String input, String output){

        String XSLFILE = XSL_FILE_PATH + xslFile;
        String INFILE = SRC_PATH + input;
        String OUTFILE = SRC_PATH + output;

        StreamSource xslcode = new StreamSource(new File(XSLFILE));
        StreamSource source = new StreamSource(new File(INFILE));
        StreamResult sink = new StreamResult(new File(OUTFILE));


        TransformerFactory tf = TransformerFactory.newInstance("net.sf.saxon.TransformerFactoryImpl", null);
        Transformer trans; 
        try {
            trans = tf.newTransformer(xslcode);
            trans.transform(source, sink);
        } catch (TransformerConfigurationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (TransformerException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }


    public  void step_1_Certificate() {
    
            /*String XSLFILE = SRC_PATH + "xsl/zeiss-user.xsl";
            String INFILE = SRC_PATH + "zeiss-user.xml";
		    String OUTFILE = SRC_PATH + "prod-skill-constraints.xml";

            StreamSource xslcode = new StreamSource(new File(XSLFILE));
		    StreamSource input = new StreamSource(new File(INFILE));
		    StreamResult output = new StreamResult(new File(OUTFILE));


            TransformerFactory tf = TransformerFactory.newInstance("net.sf.saxon.TransformerFactoryImpl", null);
            Transformer trans; 
            try {
                trans = tf.newTransformer(xslcode);
                trans.transform(input, output);
            } catch (TransformerConfigurationException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (TransformerException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }*/

            String xslFile = "zeiss-user.xsl";
            String input = "zeiss-user.xml";
		    String output ="prod-skill-constraints.xml";

            this.transform(xslFile, input, output);

    }


    public  void step_2_Certificate() {
    
        /*String XSLFILE = SRC_PATH + "xsl/zeiss-meta.xsl";
        String INFILE = SRC_PATH + "zeiss-meta.xml";
        String OUTFILE = SRC_PATH + "userCertificate.xml";

        StreamSource xslcode = new StreamSource(new File(XSLFILE));
        StreamSource input = new StreamSource(new File(INFILE));
        StreamResult output = new StreamResult(new File(OUTFILE));


        TransformerFactory tf = TransformerFactory.newInstance("net.sf.saxon.TransformerFactoryImpl", null);
        Transformer trans; 
        try {
            trans = tf.newTransformer(xslcode);
            trans.transform(input, output);
        } catch (TransformerConfigurationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (TransformerException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }*/


        String xslFile = "zeiss-meta.xsl";
        String input = "zeiss-meta.xml";
        String output ="userCertificate.xml";

        this.transform(xslFile, input, output);

    }


    // Intermediate transformation of the userCertificate xml to a format that will allow jsonification 

    public  void step_3_Certificate() {
    
        /*String XSLFILE = SRC_PATH + "xsl/userCertificateMap.xsl";
        String INFILE = SRC_PATH + "userCertificate.xml";
        String OUTFILE = SRC_PATH + "userCertificateMap.xml";

        StreamSource xslcode = new StreamSource(new File(XSLFILE));
        StreamSource input = new StreamSource(new File(INFILE));
        StreamResult output = new StreamResult(new File(OUTFILE));


        TransformerFactory tf = TransformerFactory.newInstance("net.sf.saxon.TransformerFactoryImpl", null);
        Transformer trans; 
        try {
            trans = tf.newTransformer(xslcode);
            trans.transform(input, output);
        } catch (TransformerConfigurationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (TransformerException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }*/


        String xslFile = "userCertificateMap.xsl";
        String input = "userCertificate.xml";
        String output ="userCertificateMap.xml";

        this.transform(xslFile, input, output);
        
    }


    // final step we transform the obtained map into json format 

    public  void step_4_Certificate() {
    
        /*String XSLFILE = SRC_PATH + "xsl/xml-to-json.xsl";
        String INFILE = SRC_PATH + "userCertificateMap.xml";
        String OUTFILE = SRC_PATH + "userCertificateFinal.json";

        StreamSource xslcode = new StreamSource(new File(XSLFILE));
        StreamSource input = new StreamSource(new File(INFILE));
        StreamResult output = new StreamResult(new File(OUTFILE));


        TransformerFactory tf = TransformerFactory.newInstance("net.sf.saxon.TransformerFactoryImpl", null);
        Transformer trans; 
        try {
            trans = tf.newTransformer(xslcode);
            trans.transform(input, output);
        } catch (TransformerConfigurationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (TransformerException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }*/



        String xslFile = "xml-to-json.xsl";
        String input = "userCertificateMap.xml";
        String output ="userCertificateFinal.json";

        this.transform(xslFile, input, output);
    }


    public void to_xml(){
        this.step_1_Certificate();
        this.step_2_Certificate();
    }

    public void to_json(){
        this.to_xml();
        this.step_3_Certificate();
        this.step_4_Certificate();
    }


    Object buildFileResponse(Response response) throws IOException {

        String fileName = SRC_PATH + "userCertificateFinal.json";
        File file = new File(fileName);
        HttpServletResponse raw = response.raw();
        try (InputStream is = new FileInputStream(file)) {
            IOUtils.copyStreams(is, raw.getOutputStream());
            raw.getOutputStream().close();
            return raw;
        }
    }
        

}
