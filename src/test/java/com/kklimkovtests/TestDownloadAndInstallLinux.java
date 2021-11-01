package com.kklimkovtests;

import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.qameta.allure.Story;
import org.junit.jupiter.api.*;
import java.io.IOException;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import com.jcraft.jsch.*;

@Owner("KKlimkov")
@Layer("install")
@Feature("InstallLinux")

@TestMethodOrder(OrderAnnotation.class)
public class TestDownloadAndInstallLinux {
    public static String PlatformDirectoryName = "C:\\Users\\Public\\Autotests\\" + System.getProperty("PlatformName");
    public static String Nginx = PlatformDirectoryName + "\\nginx.tar.gz";
    public static String Mplc = PlatformDirectoryName + "\\mplc.tar.gz";
    public static String NodeJs = PlatformDirectoryName + "\\nodejs.tar.gz";
    public static String InstallSh = PlatformDirectoryName + "\\install.sh";
    public static String user = System.getProperty("UserLinuxName");
    public static String password = System.getProperty("UserLinuxPassword");
    public static String host = System.getProperty("HostLinuxIP");

    @DisplayName("Download RT from FTP")
    @Test
    @Story("Install MS4D RT")
    @Tags({@Tag("Install"),@Tag("Linux")})
    @Order(1)

    public void DownloadAndCheck() throws IOException, InterruptedException {

        String FtpUrl = null;
        if (System.getProperty("Branch").equals("1.2")) {FtpUrl=
                " ftp://ftpGuestSupport:"+System.getProperty("FtpPassword")+"@support.insat.ru/Updates/Installation/MasterSCADA%204D/RunTime/"
                        + System.getProperty("PlatformName");}
        else if (System.getProperty("Branch").equals("RC")) {FtpUrl =
                " ftp://ftpGuestSupport:"+System.getProperty("FtpPassword")+"@support.insat.ru/Dev/MasterSCADA4D/RT/1.2.RC/"
                        + System.getProperty("PlatformName");}
        else if (System.getProperty("Branch").equals("Beta")) {FtpUrl =
                " ftp://ftpGuestSupport:"+System.getProperty("FtpPassword")+"@support.insat.ru/Dev/MasterSCADA4D/Beta_RT/"
                        + System.getProperty("PlatformName");}

        Steps.CreateDirectory(PlatformDirectoryName);

        String command1 = "cmd /c curl.exe -o " + Nginx + FtpUrl+ "/nginx.tar.gz";
        String command2 = "cmd /c curl.exe -o " + Mplc + FtpUrl+ "/mplc.tar.gz";
        String command3 = "cmd /c curl.exe -o " + NodeJs + FtpUrl + "/nodejs.tar.gz";
        String command4 = "cmd /c curl.exe -o " + InstallSh + FtpUrl + "/install.sh";

        Steps.LogProcess(command1,"nginx");
        Steps.LogProcess(command2,"mplc");
        Steps.LogProcess(command3,"nodejs");
        Steps.LogProcess(command4,"install.sh");
    }

    @DisplayName("Send Install Files via SSH")
    @Test
    @Story("Install MS4D RT")
    @Tags({@Tag("Install"),@Tag("Linux")})
    @Order(2)

    public void SendFiles() throws IOException, JSchException {

       Steps.ConnectSSH(user,password,host);
       Steps.SendViaSSH(Mplc, "/tmp/mplc.tar.gz");
       Steps.SendViaSSH(Nginx, "/tmp/nginx.tar.gz");
       Steps.SendViaSSH(NodeJs, "/tmp/nodejs.tar.gz");
       Steps.SendViaSSH(InstallSh, "/tmp/install.sh");
       String command = "test -f /tmp/mplc.tar.gz -a -f /tmp/nginx.tar.gz -a -f /tmp/nodejs.tar.gz -a -f /tmp/install.sh; echo $?";
       Steps.ExecuteCommandSSH(command,"1");
    }

    @DisplayName("InstallOnLinux")
    @Test
    @Story("Install MS4D RT")
    @Tags({@Tag("Install"),@Tag("Linux")})
    @Order(3)
    public void TestInstall() throws IOException {
        String command2 = "cd /tmp; sudo chmod u+x ./install.sh; sudo bash ./install.sh "+ System.getProperty("InstallOption1") + " " + System.getProperty("InstallOption2") + " "+ System.getProperty("InstallOption3") ;
        Steps.ExecuteCommandSSH(command2,"Starting MasterPLC...   BAD");
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @DisplayName("RestartMPLCLinux")
    @Test
    @Story("Install MS4D RT")
    @Tags({@Tag("Install"),@Tag("Linux")})
    @Order(4)
    public void TestRestart() throws IOException, JSchException {

        String command3 = "sudo /etc/init.d/mplc4 restart";
        Steps.ExecuteCommandSSH(command3,"Starting MasterPLC...   BAD");
        Steps.CloseSSH();
    }

}
