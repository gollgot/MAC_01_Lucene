/*
 * Author: Robin Demarta, Loïc Dessaules
 * File: QueriesPerformer.java
 * Date: 18.10.2020
 */

package ch.heigvd.iict.dmg.labo1.queries;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.misc.HighFreqTerms;
import org.apache.lucene.misc.TermStats;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;

public class QueriesPerformer {

	private Analyzer		analyzer		= null;
	private IndexReader 	indexReader 	= null;
	private IndexSearcher 	indexSearcher 	= null;

	public QueriesPerformer(Analyzer analyzer, Similarity similarity) {
		this.analyzer = analyzer;
		Path path = FileSystems.getDefault().getPath("index");
		Directory dir;
		try {
			dir = FSDirectory.open(path);
			this.indexReader = DirectoryReader.open(dir);
			this.indexSearcher = new IndexSearcher(indexReader);
			if(similarity != null)
				this.indexSearcher.setSimilarity(similarity);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void printTopRankingTerms(String field, int numTerms) {
		try {
			TermStats[] famousAuthors = HighFreqTerms.getHighFreqTerms(
					indexReader,
					numTerms,
					field,
					(o1, o2) -> {
						return (int)(o1.totalTermFreq - o2.totalTermFreq);
					}
			);

			System.out.println("Top "+ numTerms +" ranking terms for field [" + field + "] are: ");
			for(int i = 0; i < famousAuthors.length; ++i) {
				System.out.println("\t" + (i + 1) +". " + famousAuthors[i].termtext.utf8ToString()
						+ " (" + famousAuthors[i].totalTermFreq + ")");
			}
			System.out.println("");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void query(String q) throws ParseException, IOException {
		// TODO student
		// See "Searching" section

		System.out.println("Searching for " + q);

		// Query parser
		QueryParser parser = new QueryParser("summary", analyzer);
		Query query = parser.parse(q);

		// Index reader and searcher
		Path path = FileSystems.getDefault().getPath("index");
		Directory dir = FSDirectory.open(path);

		// Search query and display results
		ScoreDoc[] hits = indexSearcher.search(query, 1000).scoreDocs;
		System.out.println(hits.length + " Results, top 10: ");
		int i = 0;
		for(ScoreDoc hit : hits) {
			if(i >= 10) {
				break;
			}
			Document doc = indexSearcher.doc(hit.doc);
			System.out.println(doc.get("id") + ": "	+ doc.get("title") + " (" + hit.score + ")");
			++i;
		}
		System.out.println("");

		dir.close();
	}
	 
	public void close() {
		if(this.indexReader != null)
			try { this.indexReader.close(); } catch(IOException e) { /* BEST EFFORT */ }
	}
	
}
