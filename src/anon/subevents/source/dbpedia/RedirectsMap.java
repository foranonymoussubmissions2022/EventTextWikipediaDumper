package anon.subevents.source.dbpedia;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;

import anon.subevents.pipeline.Config;

public class RedirectsMap {

	private Map<String, String> redirects = new HashMap<String, String>();

	public void load() {
		System.out.println("Load RedirectsMap: Start.");

		String fileName = Config.getValue("data_folder") + "redirects_en.csv";

		LineIterator it = null;
		try {
			it = FileUtils.lineIterator(new File(fileName), "UTF-8");
			while (it.hasNext()) {
				String line = it.nextLine();
				String[] parts = line.split(" ");
				String source = parts[0];
				String target = parts[1];
				redirects.put(source, target);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				it.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		System.out.println("Load RedirectsMap: Done.");
	}

	public String getRedirectedPage(String source) {
		String target = this.redirects.get(source.replace(" ", "_"));
		if (target == null)
			return source;
		else
			return target;
	}

}
