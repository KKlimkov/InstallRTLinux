import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import com.jcraft.jsch.*;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

@TestMethodOrder(OrderAnnotation.class)
public class TestDownloadAndInstallLinux {
    String PlatformDirectoryName = "C:\\Users\\kiril\\Desktop\\Autotests\\InstallDirectory\\" + System.getProperty("PlatformName");
    String Nginx = PlatformDirectoryName + "\\nginx.tar.gz";
    String Mplc = PlatformDirectoryName + "\\mplc.tar.gz";
    String NodeJs = PlatformDirectoryName + "\\nodejs.tar.gz";
    String InstallSh = PlatformDirectoryName + "\\install.sh";
    File file1 = new File(Nginx);
    File file2 = new File(Mplc);
    File file3 = new File(NodeJs);
    File file4 = new File(InstallSh);
    String command1 = "cmd /c curl.exe -o " + Nginx + " ftp://ftpGuestSupport:21B74F6E@support.insat.ru/Updates/Installation/MasterSCADA%204D/RunTime/" + System.getProperty("PlatformName") + "/nginx.tar.gz";
    String command2 = "cmd /c curl.exe -o " + Mplc + " ftp://ftpGuestSupport:21B74F6E@support.insat.ru/Updates/Installation/MasterSCADA%204D/RunTime/" + System.getProperty("PlatformName") + "/mplc.tar.gz";
    String command3 = "cmd /c curl.exe -o " + NodeJs + " ftp://ftpGuestSupport:21B74F6E@support.insat.ru/Updates/Installation/MasterSCADA%204D/RunTime/" + System.getProperty("PlatformName") + "/nodejs.tar.gz";
    String command4 = "cmd /c curl.exe -o " + InstallSh + " ftp://ftpGuestSupport:21B74F6E@support.insat.ru/Updates/Installation/MasterSCADA%204D/RunTime/" + System.getProperty("PlatformName") + "/install.sh";
    String user = System.getProperty("UserLinuxName");
    ; // username for remote host
    String password = System.getProperty("UserLinuxPassword");
    ; // password of the remote host
    String host = System.getProperty("HostLinuxIP");

    public static String LogProcess(String command,String Name) throws IOException {
        String err;
        Boolean Ex = false;
        String line = "";
        Process p1 = Runtime.getRuntime().exec(command);
        InputStream stderr = p1.getErrorStream();
        InputStream stdout = p1.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(stdout));
        BufferedReader error = new BufferedReader(new InputStreamReader(stderr));
        String Out = null;
        System.out.println("AttemptGet" + Name);
        //new Thread() {
        //public void run () {
        try {
            while ((err = error.readLine()) != null) {
                System.out.println("[Stderr] " + err);
                if (err.contains("Could not resolve host") || err.contains("Recv failure")) {
                    Ex = true;
                } else {
                    Ex = false;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        while ((line = reader.readLine()) != null)
            Out = "[Stdout] " + line;
        return Out;
    }

    public static String LogProcessSSH(String user, String password, String host, String command) throws IOException {
        String line = "";
        Boolean Ex = false;
        String Out = null;
        try {
            JSch jsch = new JSch();
            java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");

            Session session = jsch.getSession(user, host, 22);
            session.setConfig(config);
            session.setPassword(password);
            session.connect();

            Channel channel = session.openChannel("exec");
            ((ChannelExec) channel).setCommand(command);
            channel.setInputStream(null);
            ((ChannelExec) channel).setErrStream(System.err);
            InputStream input = channel.getInputStream();
            channel.connect();

            System.out.println("Channel Connected to machine " + host + " server with command: " + command);

            try {
                InputStreamReader inputReader = new InputStreamReader(input);
                BufferedReader bufferedReader = new BufferedReader(inputReader);

                while ((line = bufferedReader.readLine()) != null) {
                    Ex = !Boolean.parseBoolean(line);
                    Out = line;
                    System.out.println(line);
                }
                bufferedReader.close();
                inputReader.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            //return line;
            channel.disconnect();
            session.disconnect();
            assertTrue(Ex);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return Out;
    }


    @BeforeEach
    public void TestDownloadWin() {

        File directory = new File(PlatformDirectoryName);

        if (directory.exists() && directory.isFile()) {
            System.out.println("The dir with name could not be" +
                    " created as it is a normal file");
        } else {
            if (!directory.exists()) {
                directory.mkdir();
            }
        }
    }

    @DisplayName("Download RT from FTP")
    @Test
    @Tag("DownloadFTP")
    @Order(1)
    public void DownloadAndCheck() throws IOException {
        LogProcess(command1,"nginx");
        LogProcess(command2,"mplc");
        LogProcess(command3,"nodejs");
        LogProcess(command4,"install.sh");

        try {
            System.out.println(file1.getCanonicalPath() + " " + file1.exists());
        } catch (IOException e) {
            e.printStackTrace();
        }
        Boolean Exs1 = file1.exists();
        Boolean Exs2 = file2.exists();
        Boolean Exs3 = file3.exists();
        Boolean Exs4 = file4.exists();
        assertTrue(Exs1 || Exs2 || Exs3 || Exs4);
    }

    @DisplayName("Send Install Files via SSH")
    @Test
    @Tag("SendToLinux")
    @Order(2)

    public void SendFiles() throws IOException {

        try {
            JSch jsch = new JSch();
            java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");

            Session session = jsch.getSession(user, host, 22);
            session.setConfig(config);
            session.setPassword(password);
            session.connect();

            ChannelSftp sftpChannel = (ChannelSftp) session.openChannel("sftp");
            sftpChannel.connect();
            sftpChannel.put(Mplc, "/tmp/mplc.tar.gz");
            sftpChannel.put(Nginx, "/tmp/nginx.tar.gz");
            sftpChannel.put(NodeJs, "/tmp/nodejs.tar.gz");
            sftpChannel.put(InstallSh, "/tmp/install.sh");
            sftpChannel.disconnect();
            session.disconnect();
            session.disconnect();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        String command = "test -f /tmp/mplc.tar.gz -a -f /tmp/nginx.tar.gz -a -f /tmp/nodejs.tar.gz -a -f /tmp/install.sh; echo $?";

        LogProcessSSH(user, password, host, command);
    }


    @DisplayName("InstallOnLinux")
    @Test
    @Tag("InstallLinux")
    @Order(3)
    public void TestInstall() throws IOException {
        String command2 = "cd /tmp; sudo chmod u+x ./install.sh; sudo bash ./install.sh "+ System.getProperty("InstallOption");
        String result = LogProcessSSH(user, password, host, command2);
        assertFalse(result.contains("Starting MasterPLC...   BAD"));
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @DisplayName("RestartMPLCLinux")
    @Test
    @Tag("RestartMPLCLinux")
    @Order(4)
    public void TestRestart() throws IOException {

        String command3 = "sudo /etc/init.d/mplc4 restart";
        String result = LogProcessSSH(user, password, host, command3);
        assertFalse(result.contains("Starting MasterPLC...   BAD"));

    }
}
