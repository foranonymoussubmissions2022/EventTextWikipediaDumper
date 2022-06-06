package anon.subevents.source.wikipedia.mwdumper.articleprocessing;

import java.io.BufferedWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Set;

import org.json.JSONObject;
import org.mediawiki.importer.DumpWriter;
import org.mediawiki.importer.Page;
import org.mediawiki.importer.Revision;
import org.mediawiki.importer.Siteinfo;
import org.mediawiki.importer.Wikiinfo;

import anon.subevents.meta.Language;
import anon.subevents.source.dbpedia.RedirectsMap;
import anon.subevents.source.dbpedia.TypesMap;

public class EventTextExtractor implements DumpWriter {

	String pageTitle = "";
	int _targetPageId;
	int _pageId;
	boolean debug = false;
	String _page = "";
	boolean empty = true;
	String path;
	boolean pageIsMainArticle = false;
	// Set<Integer> _pageIdsWithEvents;
	BufferedWriter fileEvents;

	private Language language;
	private RedirectsMap redirectsMap;
	private TypesMap typesMap;
	private Set<String> events;

	protected static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss.S");

	public void close() throws IOException {
	}

	public EventTextExtractor(String pageId, Language language, BufferedWriter fileEvents, RedirectsMap redirectsMap,
			TypesMap typesMap, Set<String> events) {
		this._targetPageId = Integer.parseInt(pageId);
		this.language = language;
		this.fileEvents = fileEvents;

		this.redirectsMap = redirectsMap;
		this.typesMap = typesMap;
		this.events = events;

		// this._pageIdsWithEvents = pageIdsWithEvents;
	}

	public void writeStartWiki(Wikiinfo info) throws IOException {
	}

	public void writeEndWiki() throws IOException {
	}

	public void writeSiteinfo(Siteinfo info) throws IOException {
	}

	public void writeStartPage(Page page) throws IOException {
		this.empty = true;
		this._pageId = page.Id;
		this.pageTitle = page.Title.Text;
		if (page.Ns == 0 && !page.isRedirect) {
			this.pageIsMainArticle = true;
		}
	}

	public void writeEndPage() throws IOException {
		if (this._pageId == this._targetPageId || this._targetPageId == -1) {
			this.fileEvents.flush();
		}
		this.pageIsMainArticle = false;
	}

	public void writeRevision(Revision revision) throws IOException {

		if (this.pageIsMainArticle) {

			if (this.events.contains(pageTitle.replace(" ", "_"))) {

				System.out.println("Wiki Page: " + String.valueOf(this._pageId) + ": " + this.pageTitle);

				TextExtractorNew extractor = new TextExtractorNew(revision.Text, this._pageId, true, language,
						this.pageTitle, this.redirectsMap, this.typesMap);
				try {
					extractor.extractLinks();
				} catch (Exception e) {
					System.err.println("Error (a) with " + this._pageId + ": " + this.pageTitle);
					System.err.println(e.getMessage() + "\n" + e.getStackTrace());
				}

				// String prefix = this._pageId + "\t" + this.pageTitle + "\t";

				JSONObject result = extractor.getArticleJSON();
				this.fileEvents.append(result.toString() + "\n");

//				Output output = extractor.getOutput();

				// extract events

//				EventExtractorFromYearPages eventsExtractor = new EventExtractorFromYearPages(revision.Text,
//						this._pageId, this.pageTitle, language, redirects);
//				if (eventsExtractor.isYearOrDayPage()) {
//					System.out.println("Date page: " + String.valueOf(this._pageId) + ": " + this.pageTitle);
//					eventsExtractor.extractEvents();
//					if (!eventsExtractor.getEventsOutput().isEmpty()) {
//						this.fileEvents.append(eventsExtractor.getEventsOutput() + "\n");
//					}
//				}

			}
		}
	}
}
