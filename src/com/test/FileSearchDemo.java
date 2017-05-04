package com.test;

import java.io.File;
import java.nio.file.FileSystems;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.search.highlight.SimpleSpanFragmenter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.wltea.analyzer.lucene.IKAnalyzer;

import com.test.util.FileUtil;

/**
 * 基于Lucene5.5.4的文件搜索demo
 * @author liuxianan
 * @date 2017-05-02
 */
public class FileSearchDemo
{
	public static final String INDEX_PATH = "E:\\lucene"; // 存放Lucene索引文件的位置
	public static final String SCAN_PATH = "E:\\text"; // 需要被扫描的位置，测试的时候记得多在这下面放一些文件
	
	/**
	 * 创建索引
	 */
	public void creatIndex()
	{
		IndexWriter indexWriter = null;
		try
		{
			Directory directory = FSDirectory.open(FileSystems.getDefault().getPath(INDEX_PATH));
			//Analyzer analyzer = new StandardAnalyzer();
			Analyzer analyzer = new IKAnalyzer(true);
			IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer);
			indexWriter = new IndexWriter(directory, indexWriterConfig);
			indexWriter.deleteAll();// 清除以前的index
			// 获取被扫描目录下的所有文件，包括子目录
			List<File> files = FileUtil.listAllFiles(SCAN_PATH);
			for(int i=0; i<files.size(); i++)
			{
				Document document = new Document();
				File file = files.get(i);
				document.add(new Field("content", FileUtil.readFile(file.getAbsolutePath()), TextField.TYPE_STORED));
				document.add(new Field("fileName", file.getName(), TextField.TYPE_STORED));
				document.add(new Field("filePath", file.getAbsolutePath(), TextField.TYPE_STORED));
				document.add(new Field("updateTime", file.lastModified()+"", TextField.TYPE_STORED));
				indexWriter.addDocument(document);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				if(indexWriter != null) indexWriter.close();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * 搜索
	 */
	public void search(String keyWord)
	{
		DirectoryReader directoryReader = null;
		try
		{
			// 1、创建Directory
			Directory directory = FSDirectory.open(FileSystems.getDefault().getPath(INDEX_PATH));
			// 2、创建IndexReader
			directoryReader = DirectoryReader.open(directory);
			// 3、根据IndexReader创建IndexSearch
			IndexSearcher indexSearcher = new IndexSearcher(directoryReader);
			// 4、创建搜索的Query
			// Analyzer analyzer = new StandardAnalyzer();
			Analyzer analyzer = new IKAnalyzer(true); // 使用IK分词
			
			// 简单的查询，创建Query表示搜索域为content包含keyWord的文档
			//Query query = new QueryParser("content", analyzer).parse(keyWord);
			
			String[] fields = {"fileName", "content"}; // 要搜索的字段，一般搜索时都不会只搜索一个字段
			// 字段之间的与或非关系，MUST表示and，MUST_NOT表示not，SHOULD表示or，有几个fields就必须有几个clauses
			BooleanClause.Occur[] clauses = {BooleanClause.Occur.SHOULD, BooleanClause.Occur.SHOULD};
			// MultiFieldQueryParser表示多个域解析， 同时可以解析含空格的字符串，如果我们搜索"上海 中国" 
			Query multiFieldQuery = MultiFieldQueryParser.parse(keyWord, fields, clauses, analyzer);
			
			// 5、根据searcher搜索并且返回TopDocs
			TopDocs topDocs = indexSearcher.search(multiFieldQuery, 100); // 搜索前100条结果
			System.out.println("共找到匹配处：" + topDocs.totalHits); // totalHits和scoreDocs.length的区别还没搞明白
			// 6、根据TopDocs获取ScoreDoc对象
			ScoreDoc[] scoreDocs = topDocs.scoreDocs;
			System.out.println("共找到匹配文档数：" + scoreDocs.length);
			
			QueryScorer scorer = new QueryScorer(multiFieldQuery, "content");
			// 自定义高亮代码
			SimpleHTMLFormatter htmlFormatter = new SimpleHTMLFormatter("<span style=\"backgroud:red\">", "</span>");
			Highlighter highlighter = new Highlighter(htmlFormatter, scorer);
			highlighter.setTextFragmenter(new SimpleSpanFragmenter(scorer));
			for (ScoreDoc scoreDoc : scoreDocs)
			{
				// 7、根据searcher和ScoreDoc对象获取具体的Document对象
				Document document = indexSearcher.doc(scoreDoc.doc);
				//TokenStream tokenStream = new SimpleAnalyzer().tokenStream("content", new StringReader(content));
				//TokenSources.getTokenStream("content", tvFields, content, analyzer, 100);
				//TokenStream tokenStream = TokenSources.getAnyTokenStream(indexSearcher.getIndexReader(), scoreDoc.doc, "content", document, analyzer);
				//System.out.println(highlighter.getBestFragment(tokenStream, content));
				System.out.println("-----------------------------------------");
				System.out.println(document.get("fileName") + ":" + document.get("filePath"));
				System.out.println(highlighter.getBestFragment(analyzer, "content", document.get("content")));
				System.out.println("");
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				if(directoryReader != null) directoryReader.close();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	public static void main(String args[])
	{
		FileSearchDemo demo = new FileSearchDemo();
		demo.creatIndex();
		demo.search("读取 导出");
	}
}