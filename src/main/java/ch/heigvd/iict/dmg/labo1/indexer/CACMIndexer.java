/*
 * Author: Robin Demarta, Loïc Dessaules
 * File: CACMIndexer.java
 * Date: 18.10.2020
 */

package ch.heigvd.iict.dmg.labo1.indexer;

import ch.heigvd.iict.dmg.labo1.parsers.ParserListener;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.IndexableFieldType;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.w3c.dom.Text;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;

public class CACMIndexer implements ParserListener {
	
	private Directory 	dir 			= null;
	private IndexWriter indexWriter 	= null;
	
	private Analyzer 	analyzer 		= null;
	private Similarity 	similarity 		= null;
	
	public CACMIndexer(Analyzer analyzer, Similarity similarity) {
		this.analyzer = analyzer;
		this.similarity = similarity;
	}
	
	public void openIndex() {
		// 1.2. create an index writer config
		IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
		iwc.setOpenMode(OpenMode.CREATE); // create and replace existing index
		iwc.setUseCompoundFile(false); // not pack newly written segments in a compound file: 
		//keep all segments of index separately on disk
		if(similarity != null)
			iwc.setSimilarity(similarity);
		// 1.3. create index writer
		Path path = FileSystems.getDefault().getPath("index");
		try {
			this.dir = FSDirectory.open(path);
			this.indexWriter = new IndexWriter(dir, iwc);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void onNewDocument(Long id, String authors, String title, String summary) {
		Document doc = new Document();

		// TODO student: add to the document "doc" the fields given in
		// parameters. You job is to use the right Field and FieldType
		// for these parameters.

		// Publication ID
		doc.add(new LongPoint("id", id));
		doc.add(new StoredField("id", id)); // To store a LongPoint, add separate StoreField

		// Author(s)
		if(authors != null && !authors.isEmpty()) {
			String[] authorsTokens = authors.split(";");
			for( String author : authorsTokens)
				doc.add(new StringField("authors", author, Field.Store.YES));
		}

		// Title
		doc.add(new TextField("title", title, Field.Store.YES));
		
		// Summary
		if(summary != null) {
			FieldType fieldType = new FieldType();
			fieldType.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS);
			fieldType.setStored(true);
			fieldType.setStoreTermVectors(true);
			fieldType.setStoreTermVectorOffsets(true);
			fieldType.setStoreTermVectorPositions(true);

			// Use Field instead of TextField in order to access term vector
			Field field = new Field("summary", summary, fieldType);
			doc.add(field);
		}


		try {
			this.indexWriter.addDocument(doc);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void finalizeIndex() {
		if(this.indexWriter != null)
			try { this.indexWriter.close(); } catch(IOException e) { /* BEST EFFORT */ }
		if(this.dir != null)
			try { this.dir.close(); } catch(IOException e) { /* BEST EFFORT */ }
	}
	
}
