package com.kklimkovtests;

import com.jcraft.jsch.*;
import io.qameta.allure.Step;
import org.junit.jupiter.api.Assertions;
import java.io.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Steps {


    public static Session session;

    @Step("Создание директории или файла и проверка на их создание")
    public static void CreateDirectory(String DirectoryPath) throws InterruptedException {
        File directory = new File(DirectoryPath);
        if (directory.exists() && directory.isFile()) {
            System.out.println("The dir with name could not be" +
                    " created as it is a normal file");
        } else {
            if (!directory.exists()) {
                directory.mkdir();
            }
        }
        try {
            System.out.println(directory.getCanonicalPath() + " " + directory.exists());
        } catch (IOException e) {
            e.printStackTrace();
        }
        Assertions.assertTrue(directory.exists());
    }

    @Step("Скачивание файла с ftp сервера")
    public static void LogProcess(String command,String Name) throws IOException {
        String log;
        Process p1 = Runtime.getRuntime().exec(command);
        InputStream stdlog = p1.getErrorStream();
        InputStream stdout = p1.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(stdout));
        BufferedReader error = new BufferedReader(new InputStreamReader(stdlog));
        System.out.println("AttemptGet" + Name);
        try {
            while ((log = error.readLine()) != null) {
                System.out.println("[Stderr] " + log);
                assertFalse(log.contains("Could not resolve host") || log.contains("Recv failure"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Step("Подключение по SSH")
    public static void ConnectSSH(String user, String password, String host) throws IOException, JSchException {
        try {
            JSch jsch = new JSch();
            java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");

            session = jsch.getSession(user, host, 22);
            session.setConfig(config);
            session.setPassword(password);
            session.connect();
            System.out.println("Channel Connected to machine " + host);
        } catch (JSchException e) {
            e.printStackTrace();
        }
    }

    @Step("Выполнение команды по SSH")
    public static void ExecuteCommandSSH(String command, String Result) throws IOException {
        String line = "";
        Boolean Ex = false;
        try {
            Channel channel = session.openChannel("exec");
            ((ChannelExec) channel).setCommand(command);
            channel.setInputStream(null);
            ((ChannelExec) channel).setErrStream(System.err);
            InputStream input = channel.getInputStream();
            channel.connect();
            try {
                InputStreamReader inputReader = new InputStreamReader(input);
                BufferedReader bufferedReader = new BufferedReader(inputReader);
                while ((line = bufferedReader.readLine()) != null) {
                    Ex = !Boolean.parseBoolean(line);
                    System.out.println(line);
                    assertFalse(line.contains(Result));
                }
                bufferedReader.close();
                inputReader.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            channel.disconnect();
            assertTrue(Ex);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    @Step("Передача файлов по SSH")
    public static void SendViaSSH(String File, String LinuxPath) throws IOException, JSchException {
        try {
            ChannelSftp sftpChannel = (ChannelSftp) session.openChannel("sftp");
            sftpChannel.connect();
            sftpChannel.put(File, LinuxPath);
            sftpChannel.disconnect();
        } catch (JSchException | SftpException e) {
            e.printStackTrace();
        }

    }
    @Step("Закрыть соединение по SSH")
    public static void CloseSSH() throws IOException, JSchException {
            session.disconnect();
          }
}
