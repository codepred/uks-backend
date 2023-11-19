package codepred;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;


@SpringBootApplication
@EnableJpaAuditing
@EnableConfigurationProperties
@RequiredArgsConstructor
public class TutorioBackend implements CommandLineRunner {



  public static void main(String[] args) {
    SpringApplication.run(TutorioBackend.class, args);
  }



  @Override
  public void run(String... params) throws Exception {
      // actions before start application
  }

}
