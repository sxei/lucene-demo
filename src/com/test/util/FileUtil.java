package com.test.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;


/**
 * 文件操作工具类
 * 
 * @start 2013-11-08
 * @last 2014-10-22
 * @version 1.0
 * @author LXA
 */
public class FileUtil
{
	private static Logger2 log = LoggerFactory2.getLogger(FileUtil.class);

	/**
	 * 获取文件的后缀，包括前面的点，如“.jpg”，并自动转小写
	 * @param path
	 * @return
	 */
	public static String getExt(String path)
	{
		int idx = path.lastIndexOf(".");
		if (idx < 0)
			return "";
		return path.substring(idx).toLowerCase();
	}

	/**
	 * 递归删除文件或文件夹
	 * @param file
	 * @return 返回是否删除成功，如果文件本身不存在，也返回true
	 */
	public static boolean delete(File file)
	{
		boolean flag = true;// 如果文件本来就不存在那么默认就是true
		if (file.isFile())
			flag &= file.delete();
		else if (file.isDirectory())
		{
			for (File f : file.listFiles())
				flag &= delete(f);
			flag &= file.delete();
		}
		return flag;
	}

	/**
	 * 复制单个文件
	 * @param src 待复制的文件名
	 * @param dest 目标文件名
	 * @param override 如果目标文件存在，是否覆盖
	 * @return 如果复制成功返回true，否则返回false
	 */
	public static boolean copy(String src, String dest, boolean override)
	{
		File srcFile = new File(src);
		if (!srcFile.exists()) // 判断源文件是否存在
		{
			log.error("源文件不存在：{}", src);
			return false;
		}
		else if (!srcFile.isFile())
		{
			log.error("源文件不是一个文件：{}", src);
			return false;
		}
		File destFile = new File(dest); // 判断目标文件是否存在
		if (destFile.exists())
		{
			if (override) // 如果目标文件存在并允许覆盖
				destFile.delete();// 删除已经存在的目标文件，无论目标文件是目录还是单个文件
			else
			{
				log.error("已存在同名文件且不允许覆盖：{}", dest);
				return false;
			}
		}
		else
		{
			if (!destFile.getParentFile().exists()) // 如果目标文件所在目录不存在，则创建目录
			{
				if (!destFile.getParentFile().mkdirs())
				{
					log.error("输出文件夹不存在且自动创建失败：{}", dest);
					return false;// 复制文件失败：创建目标文件所在目录失败
				}
			}
		}
		// 复制文件
		int byteread = 0; // 读取的字节数
		InputStream in = null;
		OutputStream out = null;
		try
		{
			in = new FileInputStream(srcFile);
			out = new FileOutputStream(destFile);
			byte[] buffer = new byte[1024];
			while ((byteread = in.read(buffer)) != -1)
				out.write(buffer, 0, byteread);
			return true;
		}
		catch (Exception e)
		{
			log.error("复制文件失败：", e);
			return false;
		}
		finally
		{
			try
			{
				if (out != null)
					out.close();
				if (in != null)
					in.close();
			}
			catch (Exception e)
			{
				log.error("尝试关闭流时失败：", e);
				e.printStackTrace();
			}
		}
	}

	/**
	 * 复制单个文件，存在则覆盖
	 * @param src
	 * @param dest
	 * @return
	 */
	public static boolean copy(String src, String dest)
	{
		return copy(src, dest, true);
	}

	/**
	 * 剪切文件
	 * @param src
	 * @param dest
	 * @return
	 */
	public static boolean cut(String src, String dest)
	{
		return new File(src).renameTo(new File(dest));
	}

	/**
	 * 重命名文件
	 * @param src
	 * @param newName
	 * @return
	 */
	public static boolean rename(File src, String newName)
	{
		return src.renameTo(new File(src.getParent() + "\\" + newName));
	}

	/**
	 * 重命名文件
	 * @param src
	 * @param newName
	 * @return
	 */
	public static boolean rename(String src, String newName)
	{
		File file = new File(src);
		return rename(file, newName);
	}

	/**
	 * 从文件中读取内容
	 * @param filePath
	 * @return
	 */
	public static String readFile(String filePath, String encoding)
	{
		log.debug("开始读取文件:{}", filePath);
		try
		{
			FileInputStream fis = new FileInputStream(filePath);
			BufferedReader br = new BufferedReader(new InputStreamReader(fis, encoding));
			String s = "";
			StringBuffer sb = new StringBuffer();
			while ((s = br.readLine()) != null)
				sb.append(s+"\n");
			br.close();
			return sb.toString();
		}
		catch (Exception e)
		{
			log.error("读取文件失败：", e);
			return null;
		}
	}

	/**
	 * 以默认的utf-8编码读取文件
	 * @param filePath
	 * @return
	 */
	public static String readFile(String filePath)
	{
		return readFile(filePath, "utf-8");
	}

	/**
	 * 写入文件
	 * @param filePath
	 * @param text
	 * @param encoding
	 * @return
	 */
	public static boolean writeFile(String filePath, String text, String encoding)
	{
		try
		{
			FileOutputStream fos = new FileOutputStream(filePath);
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos, encoding));
			bw.append(text);
			bw.close();
			return true;
		}
		catch (Exception e)
		{
			log.error("写入文件失败：", e);
			return false;
		}
	}

	/**
	 * 写入文件，默认utf-8编码
	 * @param filePath
	 * @param text
	 * @return
	 */
	public static boolean writeFile(String filePath, String text)
	{
		return writeFile(filePath, text, "utf-8");
	}
	
	/**
	 * 遍历执行者
	 * @author LXA
	 */
	public interface TraverseExecuter
	{
		/**
		 * 遍历某个文件夹时需要内部需要执行的方法
		 * @param filePath 文件路径
		 * @param fileName 文件名
		 */
		public void execute(String filePath, String fileName);
	}
	
	/**
	 * 遍历某个文件夹执行一段操作
	 * @param filePath 文件夹路径
	 * @param fileNameFilter 筛选器，为null时不筛选
	 * @param executer 需要调用的方法
	 */
	public static void traverse(String filePath, FilenameFilter fileNameFilter, TraverseExecuter executer)
	{
		try
		{
			File root = new File(filePath);
			for(File file : root.listFiles(fileNameFilter))
			{
				String path = file.getAbsolutePath();
				if(file.isFile())
					executer.execute(path, file.getName());
				else if(file.isDirectory())
					traverse(path, fileNameFilter, executer);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * 列出某个路径下的所有文件，包括子文件夹，如果本身就是文件，那么返回自身
	 * @param filePath 需要遍历的文件路径
	 * @param fileNameFilter 文件名过滤器
	 * @return 文件集合
	 */
	public static List<File> listAllFiles(String filePath, FilenameFilter fileNameFilter)
	{
		List<File> files= new ArrayList<File>();
		try
		{
			File root = new File(filePath);
			if(!root.exists()) return files;
			if(root.isFile()) files.add(root);
			else
			{
				for(File file : root.listFiles(fileNameFilter))
				{
					if(file.isFile()) files.add(file);
					else if(file.isDirectory())
					{
						files.addAll(listAllFiles(file.getAbsolutePath(), fileNameFilter));
					}
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return files;
	}
	
	/**
	 * 列出某个路径下的所有文件，包括子文件夹，如果本身就是文件，那么返回自身
	 * @param filePath 需要遍历的文件路径
	 * @return 文件集合
	 */
	public static List<File> listAllFiles(String filePath)
	{
		return listAllFiles(filePath, null);
	}
	
	/**
	 * 遍历某个文件夹执行一段操作
	 * @param filePath 文件夹路径
	 * @param executer 需要调用的方法
	 */
	public static void traverse(String filePath, TraverseExecuter executer)
	{
		traverse(filePath, null, executer);
	}
	
	/**
	 * 遍历某个路径，删除其中的空文件夹
	 * @param filePath
	 */
	public static void deleteEmptyFolder(File file)
	{
		File[] files = file.listFiles();
		if(files == null || files.length == 0)
		{
			delete(file);
			log.info("以下文件夹为空，已删除："+file.getAbsolutePath());
		}
		for(File f : files)
		{
			if(f.isDirectory())
				deleteEmptyFolder(f);
		}
	}
	
	/**
	 * 遍历某个路径，删除其中的空文件夹
	 * @param filePath
	 */
	public static void deleteEmptyFolder(String filePath)
	{
		deleteEmptyFolder(new File(filePath));
	}

	/**
	 * 从一个输入流写入到输出流
	 * @param is 输入流
	 * @param os 输出流
	 * @param closeInput 是否关闭输入流
	 * @param closeOutput 是否关闭输出流
	 * @throws IOException
	 */
	public static void writeIO(InputStream is, OutputStream os, Boolean closeInput, Boolean closeOutput) throws IOException
	{
		byte[] buf = new byte[1024];
		int len = -1;
		while ((len = is.read(buf)) != -1)
			os.write(buf, 0, len);
		if(closeInput)
		{
			if(is != null )
				is.close();
		}
		if(closeOutput)
		{
			if( os != null )
			{
				os.flush();
				os.close();
			}
		}
	}
	
	/**
	 * 从一个输入流写入到输出流
	 * @param is 输入流
	 * @param os 输出流
	 * @throws IOException
	 */
	public static void writeIO(InputStream is, OutputStream os) throws IOException
	{
		writeIO(is, os, false, false);
	}

}


/**
 * 为了避免过多jar包引用以及某些不需要日记记录的场合，内部写一个最简单的log工具类<br>
 * 如果复制到其他地方的时候不想带着几个log4j的jar包，可以放开本段注释<br>
 * 然后把头部的Logger和LoggerFactory都加上一个2<br>
 * 注意同一包下Logger2和LoggerFactory2不能重复<br>
 * @author LXA
 */
class Logger2
{
	public void debug(String info, Object... args)
	{
		for(int i=0; i<args.length; i++) 
			info = info.replaceFirst("\\{\\}", args[i].toString());
		System.out.println(info);
	}
	
	public void error(String info, Throwable throwable)
	{
		System.err.println(info);
		throwable.printStackTrace();
	}
	public void error(String info, String aaa)
	{
		System.err.println(info);
	}
	public void error(String info)
	{
		System.err.println(info);
	}
	public void info(String info)
	{
		System.out.println(info);
	}
}
class LoggerFactory2
{
	public static <T> Logger2 getLogger(Class<T> cls)
	{
		return new Logger2();
	}
}