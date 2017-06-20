package com.example.mutualssl;

import com.example.mutualssl.keystore.LoadKeyStore;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class MutualsslClientApplication {

	static {
		new LoadKeyStore().LoadKeyStore();
		System.setProperty("javax.net.debug", "ssl");
		javax.net.ssl.HttpsURLConnection.setDefaultHostnameVerifier(
				(hostname, sslSession) -> hostname.equals("localhost"));
	}

	@Bean
	RestTemplate restTemplate(){
		return  new RestTemplate();
	}

	public static void main(String[] args) {
		SpringApplication.run(MutualsslClientApplication.class, args);
	}
}
