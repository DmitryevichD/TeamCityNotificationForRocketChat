package rocketnotifications.teamcity.settings;

import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.ServerPaths;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.http.auth.Credentials;

public class SlackNotificationMainSettingsTest {
	SBuildServer server = mock(SBuildServer.class);
	Integer proxyPort = 8080;
	String proxyHost = "myproxy.mycompany.com";
    String defaultChannel = "#my-channel";
    String teamName =  "myteam";
    String token = "thisismytoken";
	String iconUrl = "http://www.myicon.com/icon.gif";
    String botName = "Team City";

    @After
    @Before
    public void deleteSlackConfigFile(){
        DeleteConfigFiles();
    }

    private void DeleteConfigFiles() {
        File outputFile = new File("slack", "slack-config.xml");
        outputFile.delete();

        File outputDir = new File("slack");
        outputDir.delete();
    }

    private Element getFullConfigElement(){
        return getElement("src/test/resources/main-config-full.xml");
    }

    private Element getEmptyDefaultsConfigElement(){
        return getElement("src/test/resources/main-config-empty-defaults.xml");
    }

    private Element getElement(String filePath){
        SAXBuilder builder = new SAXBuilder();
        builder.setIgnoringElementContentWhitespace(true);
        try {
            Document doc = builder.build(filePath);
            return doc.getRootElement();
        } catch (JDOMException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }
}
