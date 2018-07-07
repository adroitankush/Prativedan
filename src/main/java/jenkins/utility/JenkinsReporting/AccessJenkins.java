package jenkins.utility.JenkinsReporting;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JOptionPane;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

enum Status {
	PASSED, FAILED, PROCESSING, CANNONTDETERMINE,
}

public class AccessJenkins {
	
	private final String USER_AGENT = "Mozilla/5.0";
	
	private JsonParser jsonParser = new JsonParser();
	static DateFormat dateFormat = new SimpleDateFormat("MM/dd");
	

	public static void main(String[] args) throws Exception {
		new File("C:\\JenkinsReports").mkdir();

		AccessJenkins http = new AccessJenkins();

		String str1 = JOptionPane.showInputDialog("Enter Jenkins URL");
		String str2 = JOptionPane.showInputDialog("Enter File Name");

		String url = str1 + "/api/json?depth=0";

		System.out.println("Testing 1 - Send Http GET request");
		String response = http.sendGet(url);
		System.out.println(response.toString());
		Map<String, Status> mapJobNameAndStatus = http
				.parseJsonJobNameAndStatus(response.toString());

		int i = 0;
		HSSFWorkbook workbook = new HSSFWorkbook();
		HSSFSheet sheet = workbook.createSheet("Jenkins");
		HSSFCellStyle style = workbook.createCellStyle();
		HSSFFont font = workbook.createFont();
		font.setFontHeightInPoints((short) 10);
		font.setBoldweight(Font.BOLDWEIGHT_BOLD);
		style.setFillBackgroundColor(IndexedColors.YELLOW.getIndex());
		style.setFont(font);

		int a = 0;

		Row titleRow = sheet.createRow(i);

		Cell title1 = titleRow.createCell(a);
		title1.setCellStyle(style);
		title1.setCellValue("Job Names");

		a++;

		Cell title2 = titleRow.createCell(a);
		title2.setCellStyle(style);
		Date date = new Date();

		String date1 = dateFormat.format(date);

		title2.setCellValue("Job Status" + "-" + date1);

		a++;

		for (String jobName : mapJobNameAndStatus.keySet()) {
			System.out.println(mapJobNameAndStatus.entrySet());

			if (mapJobNameAndStatus.get(jobName).equals(Status.PASSED)) {
				System.out.println("----->" + jobName);

				int j = 0;
				i++;
				Row row = sheet.createRow(i);
				Cell cell = row.createCell(j);
				cell.setCellValue(jobName);
				j++;

				Cell cell2 = row.createCell(j);
				cell2.setCellValue("PASSED");

			}
			if (mapJobNameAndStatus.get(jobName).equals(Status.FAILED)) {
				System.out.println("----->" + jobName);

				int k = 0;
				i++;
				Row row1 = sheet.createRow(i);
				Cell cell3 = row1.createCell(k);
				cell3.setCellValue(jobName);
				k++;
				Cell cell4 = row1.createCell(k);
				cell4.setCellValue("FAILED");

			}
			if (mapJobNameAndStatus.get(jobName).equals(Status.PROCESSING)) {
				System.out.println("----->" + jobName);

				int k = 0;
				i++;
				Row row1 = sheet.createRow(i);
				Cell cell3 = row1.createCell(k);
				cell3.setCellValue(jobName);
				k++;
				Cell cell4 = row1.createCell(k);
				cell4.setCellValue("RUNNING");

			}
			if (mapJobNameAndStatus.get(jobName)
					.equals(Status.CANNONTDETERMINE)) {
				System.out.println("----->" + jobName);

				int k = 0;
				i++;
				Row row1 = sheet.createRow(i);
				Cell cell3 = row1.createCell(k);
				cell3.setCellValue(jobName);
				k++;
				Cell cell4 = row1.createCell(k);
				cell4.setCellValue("CANNOT DETERMINE");

			}

			try {

				FileOutputStream out = new FileOutputStream(new File(
						"C:\\JenkinsReports\\" + str2 + ".xls"));
				workbook.write(out);
				out.close();
				System.out.println("Excel written successfully..");

			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			} catch (IOException e1) {
				e1.printStackTrace();

			}

		}

		JOptionPane.showMessageDialog(null, "Excel Created Successfully");

	}

	private String sendGet(String url) throws Exception {

		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();

		// optional default is GET
		con.setRequestMethod("GET");

		// add request header
		con.setRequestProperty("User-Agent", USER_AGENT);

		int responseCode = con.getResponseCode();
		System.out.println("\nSending 'GET' request to URL : " + url);
		System.out.println("Response Code : " + responseCode);

		BufferedReader in = new BufferedReader(new InputStreamReader(
				con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();

		return response.toString();
	}

	@SuppressWarnings("unchecked")
	public Map<String, Status> parseJsonJobNameAndStatus(String inputJson) {
		JsonObject jsonObject = jsonParser.parse(inputJson).getAsJsonObject();
		JsonArray jsonArrayJobs = jsonObject.getAsJsonArray("jobs");
		Map<String, Status> mapJobNameStatus = new HashMap<String, Status>();
		for (int i = 0; i < jsonArrayJobs.size(); i++) {
			String nameOfTheJob = jsonArrayJobs.get(i).getAsJsonObject()
					.get("name").getAsString();
			String statusOfTheJob = jsonArrayJobs.get(i).getAsJsonObject()
					.get("color").getAsString();
			Status enumStatus = null;
			switch (statusOfTheJob) {
			case "yellow":
				enumStatus = Status.FAILED;
				break;
			case "yellow_anime":
				enumStatus = Status.PROCESSING;
				break;
			case "blue":
				enumStatus = Status.PASSED;
				break;
			case "blue_anime":
				enumStatus = Status.PROCESSING;
				break;
			case "red":
				enumStatus = Status.FAILED;
				break;
			case "red_anime":
				enumStatus = Status.PROCESSING;
				break;
			case "notbuilt":
				enumStatus = Status.CANNONTDETERMINE;
				break;
			case "aborted":
				enumStatus = Status.CANNONTDETERMINE;
				break;
			case "disabled":
				enumStatus = Status.CANNONTDETERMINE;
			}
			mapJobNameStatus.put(nameOfTheJob, enumStatus);
		}

		return mapJobNameStatus;
	}

}
