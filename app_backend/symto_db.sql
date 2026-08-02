-- MariaDB dump 10.19  Distrib 10.4.32-MariaDB, for Win64 (AMD64)
--
-- Host: localhost    Database: symto_db
-- ------------------------------------------------------
-- Server version	10.4.32-MariaDB

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `appointments`
--

DROP TABLE IF EXISTS `appointments`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `appointments` (
  `id` int(6) unsigned NOT NULL AUTO_INCREMENT,
  `user_id` int(6) unsigned NOT NULL,
  `problem_description` text DEFAULT NULL,
  `time_slot` varchar(50) DEFAULT NULL,
  `hospital_name` varchar(150) NOT NULL,
  `payment_id` varchar(100) NOT NULL,
  `status` varchar(50) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`id`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `appointments_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `appointments`
--

LOCK TABLES `appointments` WRITE;
/*!40000 ALTER TABLE `appointments` DISABLE KEYS */;
/*!40000 ALTER TABLE `appointments` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `history`
--

DROP TABLE IF EXISTS `history`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `history` (
  `id` int(6) unsigned NOT NULL AUTO_INCREMENT,
  `type` varchar(50) NOT NULL,
  `symptoms` text NOT NULL,
  `risk_level` varchar(20) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `full_response` longtext DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `history`
--

LOCK TABLES `history` WRITE;
/*!40000 ALTER TABLE `history` DISABLE KEYS */;
INSERT INTO `history` VALUES (3,'Symptom Check','I have stomach ach','Low','2026-05-23 17:00:48','{\n  \"riskLevel\": \"Low\",\n  \"riskDescription\": \"Based on your symptom of a general stomach ache without accompanying high-risk symptoms (like severe localized pain, fever, or vomiting), the risk level is currently low. However, abdominal discomfort can stem from various benign or serious causes.\",\n  \"conditions\": [\n    {\n      \"name\": \"Indigestion (Dyspepsia)\",\n      \"matchPercentage\": \"75% Match\",\n      \"description\": \"A common discomfort in the upper abdomen, often described as bloating, fullness, or a burning sensation, frequently triggered by food or stress.\"\n    },\n    {\n      \"name\": \"Gastroenteritis\",\n      \"matchPercentage\": \"60% Match\",\n      \"description\": \"An inflammation of the digestive tract, usually caused by a viral or bacterial infection, which can lead to cramps, nausea, or diarrhea.\"\n    },\n    {\n      \"name\": \"Irritable Bowel Syndrome (IBS)\",\n      \"matchPercentage\": \"40% Match\",\n      \"description\": \"A common gastrointestinal disorder characterized by recurrent abdominal pain, bloating, and changes in bowel habits.\"\n    }\n  ],\n  \"recommendations\": [\n    {\n      \"title\": \"Rest and Hydrate\",\n      \"description\": \"Sip water, clear broths, or electrolyte solutions to stay hydrated, especially if the stomach ache is accompanied by fluid loss.\",\n      \"icon\": \"check\"\n    },\n    {\n      \"title\": \"Eat Simple, Bland Foods\",\n      \"description\": \"Stick to easily digestible foods like bananas, rice, applesauce, and toast (BRAT diet). Avoid fatty, spicy, or dairy-heavy foods.\",\n      \"icon\": \"check\"\n    },\n    {\n      \"title\": \"Watch for Severe Symptoms\",\n      \"description\": \"Seek urgent medical care if you experience severe or localized pain (especially in the lower right abdomen), high fever, blood in your stool, persistent vomiting, or if the pain worsens significantly.\",\n      \"icon\": \"alert\"\n    }\n  ]\n}'),(4,'Symptom Check','Naaku Kadapa napika only','Medium','2026-05-23 17:40:25','{\n  \"riskLevel\": \"Medium\",\n  \"riskDescription\": \"You reported having stomach pain (\'kadupu noppi\'). While many cases of abdominal pain are due to minor issues like indigestion or gas, stomach pain can sometimes indicate conditions that require medical attention, especially if it worsens or is accompanied by other symptoms.\",\n  \"conditions\": [\n    {\n      \"name\": \"Indigestion (Dyspepsia)\",\n      \"matchPercentage\": \"80% Match\",\n      \"description\": \"A common condition causing pain or discomfort in the upper abdomen, often related to food intake.\"\n    },\n    {\n      \"name\": \"Gastritis\",\n      \"matchPercentage\": \"70% Match\",\n      \"description\": \"Inflammation of the protective lining of the stomach, which can cause burning pain, bloating, or nausea.\"\n    },\n    {\n      \"name\": \"Gastroenteritis\",\n      \"matchPercentage\": \"60% Match\",\n      \"description\": \"An intestinal infection (stomach flu) marked by watery diarrhea, abdominal cramps, and sometimes nausea or vomiting.\"\n    }\n  ],\n  \"recommendations\": [\n    {\n      \"title\": \"Stay Hydrated\",\n      \"description\": \"Sip water, clear broths, or electrolyte solutions (ORS) throughout the day. Avoid sugary, caffeinated, or carbonated drinks.\",\n      \"icon\": \"check\"\n    },\n    {\n      \"title\": \"Eat Bland Foods\",\n      \"description\": \"If you feel hungry, stick to simple, easy-to-digest foods like bananas, rice, applesauce, and toast (BRAT diet). Avoid spicy, oily, or very fatty foods.\",\n      \"icon\": \"check\"\n    },\n    {\n      \"title\": \"Avoid Irritating Substances\",\n      \"description\": \"Do not consume alcohol, caffeine, or smoke, as these can further irritate your stomach lining.\",\n      \"icon\": \"alert\"\n    },\n    {\n      \"title\": \"Monitor for Warning Signs\",\n      \"description\": \"Seek immediate medical attention if you experience severe, sharp pain, persistent vomiting, high fever, blood in your vomit or stool, or if the pain radiates to your back or shoulder.\",\n      \"icon\": \"alert\"\n    }\n  ]\n}'),(5,'Symptom Check','Naaku Kallu Telugu tonight','Medium','2026-05-23 17:44:20','{\n  \"riskLevel\": \"Medium\",\n  \"riskDescription\": \"You are experiencing sudden dizziness or giddiness (translated from Telugu \'Naaku Kallu Thiruguthunnayi\' / \'Kallu Telugu\'). While dizziness is often benign and related to dehydration, inner ear issues, or sudden posture changes, an acute onset tonight requires careful monitoring to ensure it is not linked to cardiovascular or neurological issues.\",\n  \"conditions\": [\n    {\n      \"name\": \"Benign Paroxysmal Positional Vertigo (BPPV)\",\n      \"matchPercentage\": \"75% Match\",\n      \"description\": \"An inner ear problem that causes a brief spinning sensation, typically triggered by moving your head.\"\n    },\n    {\n      \"name\": \"Dehydration or Low Blood Pressure\",\n      \"matchPercentage\": \"70% Match\",\n      \"description\": \"Inadequate fluid intake or a sudden drop in blood pressure can decrease blood flow to the brain, causing lightheadedness.\"\n    },\n    {\n      \"name\": \"Hypoglycemia (Low Blood Sugar)\",\n      \"matchPercentage\": \"60% Match\",\n      \"description\": \"A drop in blood sugar levels, especially if you have skipped meals, can trigger sudden dizziness and shakiness.\"\n    }\n  ],\n  \"recommendations\": [\n    {\n      \"title\": \"Sit or Lie Down Immediately\",\n      \"description\": \"To prevent falls and injury, sit or lie down flat as soon as you feel dizzy. Avoid sudden head movements.\",\n      \"icon\": \"alert\"\n    },\n    {\n      \"title\": \"Hydrate and Rest\",\n      \"description\": \"Sip water or an electrolyte solution slowly. Avoid caffeine, alcohol, or sudden changes in posture.\",\n      \"icon\": \"check\"\n    },\n    {\n      \"title\": \"Watch for Red Flag Symptoms\",\n      \"description\": \"Seek immediate emergency medical care if the dizziness is accompanied by chest pain, shortness of breath, numbness/weakness in your limbs, difficulty speaking, or a severe headache.\",\n      \"icon\": \"alert\"\n    }\n  ]\n}'),(6,'Medicine Scan','Scanned Prescription / Medicine List','Info','2026-05-23 18:04:01','{\n  \"valid\": true,\n  \"riskLevel\": \"Info\",\n  \"riskDescription\": \"Analysis of your compounded medication prescription.\",\n  \"conditions\": [\n    {\n      \"name\": \"Tr Belladonna (Tincture of Belladonna)\",\n      \"matchPercentage\": \"Prescribed\",\n      \"description\": \"An anticholinergic and antispasmodic medication used to relieve muscle spasms, cramping, and hypermotility in the gastrointestinal tract. In this prescription, 15 ml of Belladonna Tincture is compounded with an antacid.\"\n    },\n    {\n      \"name\": \"Amphojel (Aluminum Hydroxide Gel)\",\n      \"matchPercentage\": \"Prescribed\",\n      \"description\": \"An antacid used to neutralize excess stomach acid, helping to treat and prevent heartburn, indigestion, or stomach ulcers. It is added \'qsad\' (quantity sufficient to make) 120 ml of total solution.\"\n    }\n  ],\n  \"recommendations\": [\n    {\n      \"title\": \"Preparation & Administration\",\n      \"description\": \"This is a compounded liquid mixture (\'M & Ft Solution\' meaning \'mix and make a solution\'). Shake the bottle very well before each use to ensure the active ingredients are evenly distributed.\",\n      \"icon\": \"check\"\n    },\n    {\n      \"title\": \"Dosage and Timing\",\n      \"description\": \"Take 5 ml of the mixture three times a day (t.i.d.) before meals (a.c.), or as specifically directed by your physician.\",\n      \"icon\": \"check\"\n    },\n    {\n      \"title\": \"Precautions\",\n      \"description\": \"Belladonna can cause side effects like dry mouth, decreased sweating, blurred vision, or drowsiness. Avoid driving or operating machinery if you experience these effects, and consult your doctor for any concerns.\",\n      \"icon\": \"check\"\n    }\n  ]\n}');
/*!40000 ALTER TABLE `history` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `users` (
  `id` int(6) unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  `email` varchar(100) NOT NULL,
  `password` varchar(255) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (1,'Alex Johnson','alex.johnson@example.com','$2y$10$TC.9acVk0bX5JlCSPA02WeIrUW0NY92wozGNIp4nl.iASq6CEu/wW','2026-05-22 18:42:27'),(2,'vish','vish@gmail.com','$2y$10$oDPImUpPxjobcYRAFYoNMejhF7HzVCt5x0.M1iAiffCIiJAk7dNM.','2026-05-23 16:43:06');
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-05-24  0:17:52
