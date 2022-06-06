package anon.subevents.wikipedia.mwdumper;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.sql.Connection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.mediawiki.dumper.ProgressFilter;
import org.mediawiki.importer.DumpWriter;
import org.mediawiki.importer.MultiWriter;
import org.mediawiki.importer.XmlDumpReader;

import anon.subevents.meta.Language;
import anon.subevents.pipeline.Config;
import anon.subevents.source.dbpedia.RedirectsMap;
import anon.subevents.source.dbpedia.TypesMap;
import anon.subevents.source.wikipedia.mwdumper.articleprocessing.EventTextExtractor;
import anon.subevents.util.FileName;
import anon.subevents.wikipedia.WikiWords;

class Dumper {

	private static final int IN_BUF_SZ = 1024 * 1024;

	public static void main(String[] args) throws IOException, ParseException {

		if (args.length != 2)
			return;

		Config.init(args[0]);
		MultiWriter writers = new MultiWriter();

		Language language = Language.getLanguage(args[1]);

		List<Language> languages = new ArrayList<Language>();
		languages.add(language);
		WikiWords.getInstance().init(languages);

		RedirectsMap redirectsMap = new RedirectsMap();
		redirectsMap.load();
		TypesMap typesMap = new TypesMap();
		typesMap.load();

		DumpWriter sink = getEventTextExtractor(language, redirectsMap, typesMap);
		InputStream input = openStandardInput();

		writers.add(sink);

		int progressInterval = 1000;
		DumpWriter outputSink = (progressInterval > 0) ? (DumpWriter) new ProgressFilter(writers, progressInterval)
				: (DumpWriter) writers;

		XmlDumpReader reader = new XmlDumpReader(input, outputSink);
		reader.readDump();
	}

	static InputStream openStandardInput() throws IOException {
		return new BufferedInputStream(System.in, IN_BUF_SZ);
	}

	static class OutputWrapper {
		private OutputStream fileStream = null;
		private Connection sqlConnection = null;

		OutputWrapper(OutputStream aFileStream) {
			fileStream = aFileStream;
		}

		OutputWrapper(Connection anSqlConnection) {
			sqlConnection = anSqlConnection;
		}

		OutputStream getFileStream() {
			if (fileStream != null)
				return fileStream;
			if (sqlConnection != null)
				throw new IllegalArgumentException("Expected file stream, got SQL connection?");
			throw new IllegalArgumentException("Have neither file nor SQL connection. Very confused!");
		}

	}

	private static EventTextExtractor getEventTextExtractor(Language language, RedirectsMap redirectsMap,
			TypesMap typesMap) {
		BufferedWriter fileEvents = null;

		try {
			Date currentDate = new Date();
			Random random = new Random();
			long LOWER_RANGE = 0L;
			long UPPER_RANGE = 999999999L;
			long randomValue = LOWER_RANGE + (long) (random.nextDouble() * (UPPER_RANGE - LOWER_RANGE));
			String pathSuffix = language + "_" + new SimpleDateFormat("yyyyMMddhhmm").format(new Date()) + "_"
					+ currentDate.getTime() + "_" + String.valueOf(randomValue) + ".ndjson";
			String path1 = Config.getValue("data_folder") + FileName.WIKIPEDIA_TEXTUAL_EVENTS.getFileName()
					+ pathSuffix;
			System.out.println(path1);
			fileEvents = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path1, false), "UTF-8"));
		} catch (Exception e) {
			e.printStackTrace();
		}

		Set<String> events = typesMap.getResourcesOfType("Event");
		System.out.println("Number of events: " + events.size() + ".");

		return new EventTextExtractor("-1", language, fileEvents, redirectsMap, typesMap, events);
	}

}
