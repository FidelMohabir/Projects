
package com.FileProcessor.Executor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.sql.*;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.concurrent.Semaphore;


import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;



public class FileProcessorThread implements Runnable{
	private static final Semaphore sema = new Semaphore(1);
	private Path path;
	
	public FileProcessorThread(Path path) {
		this.path =  path;
		System.out.println("world");
	}
	@Override
	public void run() {
		System.out.println(".");
		try {
			
			 Class.forName("org.postgresql.Driver");
			 String url = "jdbc:postgresql://[::1]:5432/test1";
			 Properties props = new Properties();
			 props.setProperty("user", "postgres");
			 props.setProperty("password", "123");
			 props.setProperty("ss1", "true");
			 Connection con = DriverManager.getConnection(url, props);
			 
			 File file = new File(path.toString());
			 DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			 DocumentBuilder builder = factory.newDocumentBuilder();
			 Document xmlDoc = builder.parse(file.getAbsolutePath());
			 
			 XPath xpath = XPathFactory.newInstance().newXPath();
			 Object acc = xpath.evaluate("AccountList/Account", xmlDoc, XPathConstants.NODESET);
			 
			 PreparedStatement forAcc = con.prepareStatement("INSERT INTO Account(\n" +
					 						" external id, AccountNumber, AccountType, CustomerID)\n" +
					 						"VALUES(?,?,?,?)");
			 						
			 PreparedStatement forCust = con.prepareStatement("with rows as (\n" +
					 							"INSERT INTO Customer(\n" +
					 						"FirstName, LastName, Address, SSN)\n" +
					 							"VALUES(?,?,?,?)\n" +
					 								"RETURNING id)" +
					 							"INSERT INTO Account (CustomerID)\n" +
					 								"SELECT id\n" +
					 								"FROM rows");
			 
			 for (int i = 0 ; i < ((NamedNodeMap) acc).getLength() ; i++) {
				    Node node = ((NamedNodeMap) acc).item(i);
				    List<String> columns = Arrays
				    .asList(getAttrValue(node, "id"),
				        getTextContent(node, "AccountNumber"),
				        getTextContent(node, "AccountType"),
				        getTextContent(node, "Customer_Details"));
				    
				    String a, b, c, d, temp;
				    temp = columns.get(3);
				   StringTokenizer st = new StringTokenizer(temp);
				   
				    a = st.nextElement().toString();
				    b = st.nextElement().toString();
				    c = st.nextElement().toString();
				    d = st.nextElement().toString();
				    
				    forCust.setString(1, a);
				    forCust.setString(2, b);
				    forCust.setString(3, c);
				    forCust.setString(4, d);
				    
				    forCust.execute();
				    
				    
				    for (int n = 0 ; n < columns.size()-1 ; n++) {
				    forAcc.setString(n+1, columns.get(n));
				    }
				    
				    forAcc.execute();
				}
			
			 
			 
		}
		  catch(ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	

	private static String getAttrValue(Node node,String attrName) {
		if ( ! node.hasAttributes() ) return "";
		NamedNodeMap nmap = node.getAttributes();
		if ( nmap == null ) return "";
		Node n = nmap.getNamedItem(attrName);
    	if ( n == null ) return "";
    	return n.getNodeValue();
}
	
	private static String getTextContent(Node parentNode,String childName) {
	    NodeList nlist = parentNode.getChildNodes();
	    for (int i = 0 ; i < nlist.getLength() ; i++) {
	    Node n = nlist.item(i);
	    String name = n.getNodeName();
	    if ( name != null && name.equals(childName) )
	        return n.getTextContent();
	    }
	    return "";
	}

}
