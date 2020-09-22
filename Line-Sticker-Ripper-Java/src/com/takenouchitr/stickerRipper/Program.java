package com.takenouchitr.stickerRipper;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.*;
import java.net.*;
import javax.swing.*;

@SuppressWarnings("serial")
public class Program extends JFrame
{
	private static final String REGEX_URL = "^https://store.line.me/stickershop/product/\\d+/*\\w*$";
	private static final String REGEX_TITLE = "<p class=\"mdCMN38Item01Ttl\">(.*)</p>[\\s]*</div>"
			+ "[\\s]*<a class=\"mdCMN38Item01Author";
	private static final String REGEX_IMAGE = "background-image:url\\(([\\w\\d:/\\.\\-]*);";
	private static final String REGEX_ILLEGAL = "([\\/:*?\"<>|])";
	
	private JTextField txt_url;
	private JLabel lbl_title, lbl_count;

	public static void main(String[] args)
	{
		File folder = new File("stickers/");
		if (!folder.exists())
			folder.mkdir();
		
		Program frame = new Program();
		frame.setVisible(true);
	}
	
	public Program()
	{
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setResizable(false);
		setSize(380, 117);
		setTitle("Line Sticker Ripper");
		getContentPane().setLayout(null);
		
		JLabel lbl_url = new JLabel("URL");
		lbl_url.setBounds(10, 11, 33, 14);
		getContentPane().add(lbl_url);
		
		txt_url = new JTextField();
		txt_url.setBounds(53, 8, 311, 20);
		getContentPane().add(txt_url);
		txt_url.setColumns(10);
		
		JButton btn_download = new JButton("Download");
		btn_download.addActionListener((obj) -> downloadPress());
		btn_download.setMnemonic('d');
		btn_download.setBounds(258, 54, 106, 23);
		getContentPane().add(btn_download);
		
		lbl_title = new JLabel("Title:");
		lbl_title.setBounds(10, 36, 160, 14);
		getContentPane().add(lbl_title);
		
		lbl_count = new JLabel("Sticker Count:");
		lbl_count.setBounds(10, 61, 160, 14);
		getContentPane().add(lbl_count);
		
	}

	private void downloadPress()
	{
		Pattern urlPattern = Pattern.compile(REGEX_URL);
		Matcher urlMatch = urlPattern.matcher(txt_url.getText());
		
		if (!urlMatch.find())
		{
			JOptionPane.showMessageDialog(null, "URL could not be verified.\n"
					+ "Please make sure that you pasted in the entire URL.\n"
					+ "(https://store.line.me/stickershop/product/<id>/)", "URL not verified", 
					JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		
		StringBuilder data = new StringBuilder();
		
		try
		{
			URL url = new URL(txt_url.getText());
			
			BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
			
			String line;
			while ((line = in.readLine()) != null)
				data.append(line);
			
			in.close();
		} 
		catch (MalformedURLException e)
		{
			// When the URL is bad
			e.printStackTrace();
		} 
		catch (IOException e)
		{
			// When the Stream gets borked
			e.printStackTrace();
		}
		
		String dataString = data.toString();
		
		Pattern titlePattern = Pattern.compile(REGEX_TITLE);
		Pattern imagePattern = Pattern.compile(REGEX_IMAGE);
		Matcher titleMatch = titlePattern.matcher(dataString);
		Matcher imageMatch = imagePattern.matcher(dataString);
		
		List<String> files = new ArrayList<>();
		while (imageMatch.find())
			files.add(imageMatch.group(1));
		
		titleMatch.find();
		imageMatch.find();
		
		String title = titleMatch.group(1);
		//title = title.replaceAll(REGEX_ILLEGAL, " ");
		
		lbl_title.setText("Title: " + title);
		lbl_count.setText("Sticker Count: " + (files.size() / 2));
		
		String threadTitle = title;
		
		Thread downloadThread = new Thread() 
		{
			@Override
			public void run()
			{
				startDownload(dataString, threadTitle, files);
			}
		};
		
		downloadThread.start();
	}
	
	private void startDownload(String data, String title, List<String> files)
	{
		File packFolder = new File("stickers/" + title + "/");
		if (!packFolder.exists())
			packFolder.mkdir();
		
		List<Thread> threads = new ArrayList<>();
		
		for (int i = 0; i < files.size() / 2; i++) 
		{
			String imageURL = files.get(i * 2);
			int imageIndex = i;
			
			Thread thread = new Thread() 
			{
				@Override
				public void run()
				{
					downloadImage(imageURL, imageIndex, title);
				}
			};
			
			thread.start();
			threads.add(thread);
		}
		
		for (Thread t : threads)
		{
			try
			{
				t.join();
			} 
			catch (InterruptedException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		JOptionPane.showMessageDialog(null, String.format("%1$d stickers have been downloaded into stickers/%2$s", 
				files.size() / 2, title), "Download complete!", JOptionPane.INFORMATION_MESSAGE);
	}
	
	private void downloadImage(String imageURL, int count, String title)
	{
		InputStream in = null;
		OutputStream out = null;
		
		try
		{
			URL url = new URL(imageURL);
			in = url.openStream();
			out = new FileOutputStream(String.format("stickers/%1$s/%2$d.png", title, count));
			
			byte[] buffer = new byte[2048];
            int length;
 
            while ((length = in.read(buffer)) != -1) {
                out.write(buffer, 0, length);
            }
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			if (in != null)
			{
				try
				{
					in.close();
				} 
				catch (IOException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (out != null)
			{
				try
				{
					out.close();
				} 
				catch (IOException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
}
