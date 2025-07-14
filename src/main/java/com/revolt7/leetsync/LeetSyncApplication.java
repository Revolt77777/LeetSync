package com.revolt7.leetsync;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class LeetSyncApplication {

	public static void main(String[] args) {
		SpringApplication.run(LeetSyncApplication.class, args);
	}
	/*
	@Autowired
	private LeetCodeService leetCodeService;

//	@Autowired
//	private DynamoService dynamoService;


	@Override
	public void run(String... args) throws Exception {
		int limit = 15;
		List<AcSubmission> submissions = leetCodeService.fetchRecentAcceptedSubmissions("zxuanxu", limit);
		System.out.println(submissions);
		*//*for (AcSubmission submission : submissions) {
			boolean stored = dynamoService.storeIfNew(submission);
			System.out.printf("Submission: %-35s | Stored: %s%n", submission.getTitle(), stored);
		}

		System.out.println("✅ Sync complete.");
	}*/
}
