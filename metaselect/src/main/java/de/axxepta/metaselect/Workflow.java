package de.axxepta.metaselect;

import de.axxepta.converterservices.proc.Pipeline;

import de.axxepta.converterservices.utils.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import javax.servlet.http.HttpServletResponse;

import spark.Response;

import static de.axxepta.metaselect.Configuration.SRC_PATH;
import static de.axxepta.metaselect.Configuration.XSL_FILE_PATH;


public class Workflow {

    private static final Logger LOGGER = LoggerFactory.getLogger(Workflow.class);

    Workflow(){}

    private Pipeline.PipelineBuilder pipelineHead() {
        return Pipeline.builder().verbose();
    }


    public void to_json(){

        Pipeline.PipelineBuilder pipelineBuilder = pipelineHead();
        pipelineBuilder.step(Pipeline.StepType.XSLT, "", SRC_PATH + "zeiss-user.xml ", SRC_PATH + "prod-skill-constraints.xml", XSL_FILE_PATH + "zeiss-user.xsl", true)
                       .step(Pipeline.StepType.XSLT, "", SRC_PATH + "zeiss-meta.xml", SRC_PATH + "userCertificate.xml", XSL_FILE_PATH + "zeiss-meta.xsl", true)
                       .step(Pipeline.StepType.XSLT, "", "", SRC_PATH + "userCertificateMap.xml", XSL_FILE_PATH + "userCertificateMap.xsl", true)
                       .step(Pipeline.StepType.XSLT, "", "", SRC_PATH + "userCertificateFinal.json", XSL_FILE_PATH + "xml-to-json.xsl", true)
                       .exec();

       
    }

    public void to_xml(){

        Pipeline.PipelineBuilder pipelineBuilder = pipelineHead();
        pipelineBuilder.step(Pipeline.StepType.XSLT, "", SRC_PATH + "zeiss-user.xml ", SRC_PATH + "prod-skill-constraints.xml", XSL_FILE_PATH + "zeiss-user.xsl", true)
                       .step(Pipeline.StepType.XSLT, "", SRC_PATH + "zeiss-meta.xml", SRC_PATH + "userCertificate.xml", XSL_FILE_PATH + "zeiss-meta.xsl", true)
                       .step(Pipeline.StepType.XSLT, "", "", SRC_PATH + "userCertificateMap.xml", XSL_FILE_PATH + "userCertificateMap.xsl", true)
                       .exec();
    }


    Object buildFileResponseJson(Response response) throws IOException {

        String fileName = SRC_PATH + "userCertificateFinal.json";
        File file = new File(fileName);
        HttpServletResponse raw = response.raw();
        try (InputStream is = new FileInputStream(file)) {
            IOUtils.copyStreams(is, raw.getOutputStream());
            raw.getOutputStream().close();
            return raw;
        }
    }

    Object buildFileResponseXml(Response response) throws IOException {

        String fileName = SRC_PATH + "userCertificate.xml";
        File file = new File(fileName);
        HttpServletResponse raw = response.raw();
        try (InputStream is = new FileInputStream(file)) {
            IOUtils.copyStreams(is, raw.getOutputStream());
            raw.getOutputStream().close();
            return raw;
        }
    }


 
}
