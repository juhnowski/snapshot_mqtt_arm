package ru.juhnowski.snapshot;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.fusesource.mqtt.client.BlockingConnection;
import org.fusesource.mqtt.client.MQTT;
import org.fusesource.mqtt.client.Message;
import org.fusesource.mqtt.client.QoS;
import org.fusesource.mqtt.client.Topic;
import org.fusesource.mqtt.client.Tracer;
import org.fusesource.mqtt.codec.MQTTFrame;
import org.fusesource.mqtt.codec.PUBLISH;

import java.util.Calendar;
import java.util.Date;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

/// workspace/app

@SpringBootApplication
public class SnapshotApplication implements CommandLineRunner {

public static String convertDateToString() {
	String pattern = "yyyyMMddHHmm";
	SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
    Date today = Calendar.getInstance().getTime();
    String str = simpleDateFormat.format(today);
    return (str);
  }

	public static void main(String[] args) {
		SpringApplication.run(SnapshotApplication.class, args);
	}

	@Override
    public void run(String... args) {
		Message message;
		Process process;
		String fn;

		try {
			MQTT mqtt = new MQTT();
			mqtt.setHost("tcp://178.140.0.246:8883");
			BlockingConnection connection = mqtt.blockingConnection();
			connection.connect();

		//	connection.publish("test", "SpringBoot Hello".getBytes(), QoS.AT_LEAST_ONCE, false);
			
			Topic[] topics = {new Topic("snapshot", QoS.AT_LEAST_ONCE)};
			byte[] qoses = connection.subscribe(topics);

			while(true) {
				message = connection.receive();
				System.out.println(message.getTopic());
				byte[] payload = message.getPayload();
				// process the message then:

				String string = new String(payload);
				//System.out.println("payload: " + string);

				File homeDirectory = new File("/workspace/app");
				String[] envp = new String[1];
				String[] envp1 = new String[2];
				//String string = "E121MM750";
				fn = string+"_"+convertDateToString()+".jpg";
				envp[0] = "/workspace/app/"+fn; //
				process = Runtime.getRuntime().exec(String.format("ffmpeg -y -loglevel fatal -rtsp_transport tcp -i rtsp://admin:ahH2OoVJLy_@192.168.0.246:1554 -frames:v 2 -r 1 -s 640x480 "+envp[0], envp, homeDirectory));
				process.waitFor();
				// System.out.println(envp[0]+": done");
				
				envp1[0] = "frm/"+string;
				envp1[1] = fn;
				process = Runtime.getRuntime().exec("./workspace/app/scripts/minio-upload.sh " + envp1[0] + " " + envp1[1],envp1, homeDirectory);
				process.waitFor();
				// System.out.println("=============================================");
				// System.out.println("/home/ilya/parking/snapshot/minio-upload.sh " + envp1[0] + " " + envp1[1]);
				// System.out.println("=============================================");
				
				process = Runtime.getRuntime().exec("rm " + envp[0],envp1, homeDirectory);
				process.waitFor();

				message.ack();	
			}
		}catch (Exception e){
			e.printStackTrace();
		}
	}

}