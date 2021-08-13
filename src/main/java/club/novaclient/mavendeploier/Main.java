package club.novaclient.mavendeploier;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Scanner;

public class Main {
    public static final ArrayList<Artifact> artifacts = new ArrayList<>();

    public static void main(String[] args) throws IOException {
        File root = new File("/home/fan87/.minecraft/libraries");
        System.out.println("Main Path: " + root.getAbsolutePath());
        System.out.println("WARNING: DOING IT MIGHT FUCK YOUR MAVEN REPO UP, YOU SURE? (it's for personally use mainly, but you can modify the code to get this work)");
        Scanner input = new Scanner(System.in);
        System.out.println("If you understand what you are doing, and you modified the code, and double checked it, please type:");
        System.out.println("\"Yes, do what I said.\"");
        if (!input.nextLine().equals("Yes, do what I said.")) {
            System.out.println("Okay. Process ended. Press ENTER to quit.");
            input.nextLine();
            System.exit(0);
            return;
        }
        System.out.println("Indexing...");
        indexFolder(root);
        for (Artifact artifact : artifacts) {
            System.out.println("Deploying Artifact: " + artifact.getName());
            Process process = Runtime.getRuntime().exec(
                    "mvn deploy:deploy-file -DgroupId=<group-id> -DartifactId=<artifact-id> -Dversion=<version> -Dpackaging=jar -Dfile=<path-to-file> -DrepositoryId=nova -Durl=http://69.69.0.1:8081/repository/nova-repository/"
                            .replaceAll("<group-id>", artifact.getGroupID())
                            .replaceAll("<artifact-id>", artifact.getArtifactID())
                            .replaceAll("<version>", artifact.getVersion())
                            .replaceAll("<path-to-file>", artifact.getPath().getAbsolutePath()));
            Scanner scanner = new Scanner(process.getInputStream());
            while (scanner.hasNextLine()) {
                System.out.println(scanner.nextLine());
            }
        }
    }

    public static void indexFolder(File folder) {
        for (File file : folder.listFiles()) {
            if (file.isDirectory()) {
                System.out.println("Found child folder: " + file.getName());
                indexFolder(file);
            }
            if (!file.isDirectory() && file.getName().endsWith("jar") && !file.getName().equalsIgnoreCase("MavenDeployer-1.0-SNAPSHOT.jar")) {
                Artifact artifact = new Artifact(file);
                artifacts.add(artifact);
                System.out.println("Found artifact: " + artifact.getName() + " - " + artifact.getPath().getName());
            }
        }
    }

    public static class Artifact {
        private File path;

        public String getName() {
            return getGroupID() + ":" + getArtifactID() + ":" + getVersion();
        }

        public Artifact(File path) {
            this.path = path;
        }

        public File getPath() {
            return path;
        }

        public String getArtifactID() {
            return path.getParentFile().getParentFile().getName();
        }
        public String getGroupID() {
            File parent = path.getParentFile().getParentFile().getParentFile();
            String groupId = "";
            while (!parent.getName().equalsIgnoreCase("libraries")) { // Track down to libraries folder, or root folder
                groupId = parent.getName() + "." + groupId;
                parent = parent.getParentFile();
            }
            groupId = groupId.substring(0, groupId.length() - 1);
            return groupId;
        }
        public String getVersion() {
            return path.getParentFile().getName();
        }
    }

}
