import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.function.Consumer;
import java.util.logging.Logger;

/*
 * Copyright 2020 looseBoxes.com
 *
 * Licensed under the looseBoxes Software License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.looseboxes.com/legal/licenses/software.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * @author USER
 */
public class Startup {

    private static final Logger LOG = Logger.getLogger(Startup.class.getName());

    public static void main(String... args){
        
        final String key = "java -jar";

        final String [] appNames = {
            "discoveryserver", "bcsafecontentservice", "cometdchatservice"
        };
        final int [] appPorts = {
            8761, 8093, 8092
        };

        final Runtime runtime = Runtime.getRuntime();

        try{
            
            final String [] props = {
                "JAVA_HOME=C:\\Program Files\\Java\\jdk1.8.0_144",
                "M2_HOME=C:\\Program Files\\Maven\\apache-maven-3.6.3"
            };
            
            for(int i=0; i<appNames.length; i++) {
                
                final String name = appNames[i];
                final int port = appPorts[i];
                final File workingDir = new File("C:\\Users\\USER\\Documents\\NetBeansProjects\\"+name);
                final String tmpDir = "C:\\Users\\USER\\Documents\\NetBeansProjects\\"+name+"\\tmp";
//                System.setProperty("java.io.tmpdir", tmpDir);
                final String [] envp = new String[props.length + 1];
                System.arraycopy(props, 0, envp, 0, props.length);
                envp[props.length] = "java.io.tmpdir="+tmpDir;
                
                final String run = "java -jar target\\"+name+"-1.0-SNAPSHOT.jar";
                final String pack = "mvn package && java -jar target/"+name+"-1.0-SNAPSHOT.jar";
                final String dockerBld = "docker build . -t "+name+":1.0";
                final String dockerRun = "docker run -p "+port+":"+port+" -t -name "+name+"-container "+name+":1.0";
                
                String target = null;
                switch(key) {
                    case "java -jar": target = run; break;
                    case "mvn package": target = pack; break;
                    case "docker build": target = dockerBld; break;
                    case "docker run": target = dockerRun; break;
                    case "shutdown": new ShutdownViaSpringBootActuator().run(port); break;
                    default: throw new IllegalArgumentException(key);
                }
                
                System.out.println("Executing: " + target);
                
                final Process process = runtime.exec(target, envp, workingDir);
                        
                new Startup().accept(process, (line) -> System.out.println(line));
            }
        }catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void accept(Process process, Consumer<String> outputHandler) {
        try{
            
            this.consume(process, outputHandler);

        }catch(IOException e) {

            e.printStackTrace();
        }
    }

    public void consume(Process process, Consumer<String> outputHandler) 
            throws IOException{

        final int outputLines = consume(process.getInputStream(), outputHandler);

        if(outputLines < 1) {

            consume(process.getErrorStream(), outputHandler);
        }
    }

    protected int consume(InputStream in, Consumer<String> outputHandler) throws IOException {

        int linesConsumed = 0;

        final boolean hasOutput = in != null;// && in.available() > 0;

//        LOG.finer(() -> "Has output: " + hasOutput);

        if(hasOutput) {

            try(final BufferedReader buf = new BufferedReader(new InputStreamReader(in))) {

                String read;

                while ((read=buf.readLine()) != null) {

                    final int lineNum = linesConsumed;
                    final String line = read;
//                    LOG.info(() -> "\t> " + lineNum + ": " + line);

                    outputHandler.accept(read);

                    ++linesConsumed;
                }   
            }
        }

        final int n =  linesConsumed;
//        LOG.info(() -> "Has output: " + hasOutput + ", lines consumed: " + n);

        return linesConsumed;
    }
}
