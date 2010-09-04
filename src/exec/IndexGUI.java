package exec;

import index.IndexInfoFrame;
import index.InvertedIndex;
import index.QueryResult;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;
import java.util.SortedSet;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.looks.plastic.Plastic3DLookAndFeel;
import com.jgoodies.looks.plastic.theme.ExperienceBlue;

/**
 * A Frame for user interaction with the index.  Displays a results pane as well as a search
 * pane.  Allows users to index parts of their filesystem using commands from the menu bar.
 * @author William Howard
 */
public class IndexGUI extends JFrame implements ActionListener {
	public static final long serialVersionUID = 0x00;
	
	private static FileFilter directoryFilter = new FileFilter() {
		public boolean accept(File dir) {
			return dir.isDirectory();
		}
	};
	
	private static FileFilter fileFilter = new FileFilter() {
		public boolean accept(File dir) {
			return dir.isFile();
		}
	};	
	
	private InvertedIndex index = null;
	
	private IndexInfoFrame info_frame = null;
	
	private DefaultMutableTreeNode root = new DefaultMutableTreeNode("Results");
	private DefaultTreeModel model = new DefaultTreeModel(root);
	private JTree tree = new JTree(model);
	
	private JTextField max_results = new JTextField("10");
	private JTextField searchField = new JTextField(10);
	private JButton searchButton = new JButton("Search");	
	
	private JToolBar toolbar = null;
	private JPanel searchPane = null;
	private JPanel statusPane = null;
	
	private static JFileChooser fc = new JFileChooser();
	
	private JTextField messageBar = new JTextField("",30);
	private JProgressBar mem_usage = new JProgressBar(0,100);
	
	private static Border empty = BorderFactory.createEmptyBorder(5,5,5,5);
	
	static {
		fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		fc.setMultiSelectionEnabled(true);
	}
	
	public IndexGUI(InvertedIndex index) {
		super("Inverted Index Search Engine");
		this.setLayout(new BorderLayout());
		
		this.index = index;
		
		this.info_frame = new IndexInfoFrame(this.index);
		this.info_frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        this.info_frame.setPreferredSize(new Dimension(300,450));
        this.info_frame.pack();
        
        Timer t = new Timer(100,this);
        t.setActionCommand("mem-update");
        t.start();

        this.add(getToolbar(),BorderLayout.NORTH);
        this.add(getSearchPane(),BorderLayout.CENTER);
        this.add(getStatusPane(),BorderLayout.SOUTH);
	}
	
	private JToolBar getToolbar() {
		if(this.toolbar == null) {
			this.toolbar = new JToolBar("toolbar");
			this.toolbar.setFloatable(false);
			this.toolbar.setRollover(true);
			
			JButton openButton = new JButton("Open File");
			openButton.setActionCommand("open");
			openButton.addActionListener(this);
			this.toolbar.add(openButton);
			
			JButton indexButton = new JButton("Index Documents");
			indexButton.setActionCommand("index");
			indexButton.addActionListener(this);
			this.toolbar.add(indexButton);
			
			JButton infoButton = new JButton("Index Information");
			infoButton.setActionCommand("info");
			infoButton.addActionListener(this);
			this.toolbar.add(infoButton);			
		}
		return toolbar;
	}
	
	private JPanel getSearchPane() {
		if(this.searchPane == null) {
			JScrollPane jsp = new JScrollPane(tree);
	        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
	        			
	        FormLayout layout0 = new FormLayout("50dlu:grow, 3dlu, left:pref");
	        DefaultFormBuilder builder0 = new DefaultFormBuilder(layout0);
	        
			searchField.addActionListener(this);
			searchField.setActionCommand("search");
			searchButton.addActionListener(this);
			searchButton.setActionCommand("search");
			
			builder0.append(searchField,searchButton);
			
			FormLayout layout = new FormLayout("right:max(60dlu;pref), 3dlu, 50dlu:grow");
			
	        DefaultFormBuilder builder = new DefaultFormBuilder(layout);
	        builder.setDefaultDialogBorder();
	        
	        builder.appendSeparator("Search Results");
	        	builder.append(jsp, 3);
	        builder.appendSeparator("Search");
	        	builder.append(builder0.getPanel(),3);
	        builder.appendSeparator("Advanced");
	        	builder.append("Max Results:",max_results);
	        	
	        this.searchPane = builder.getPanel();
		}
		return this.searchPane;
	}
	
	public JPanel getStatusPane() {
		if(this.statusPane == null) {
	        FormLayout layout0 = new FormLayout("50dlu:grow, 3dlu, left:80px");
	        DefaultFormBuilder builder0 = new DefaultFormBuilder(layout0);
	        builder0.setBorder(empty);
			
			this.messageBar.setEditable(false);
			builder0.append(this.messageBar,this.mem_usage);
			
			this.statusPane = builder0.getPanel();
		}
		return this.statusPane;
	}
	
	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();
		
		if(cmd.equals("search"))
			this.search(searchField.getText());
		else if(cmd.equals("open"))
			this.openFile();
		else if(cmd.equals("index"))
			this.makeIndex();
		else if(cmd.equals("info"))
			this.showInfo();
		else if(cmd.equals("mem-update"))
			this.calculateMemoryUsage();
	}
	
	private void makeIndex() {
    	fc.setDialogTitle("Select Files/Directories to Index");
    	if(fc.showOpenDialog(null) != JFileChooser.APPROVE_OPTION)
    		return;
    	
    	for(File f : fc.getSelectedFiles()) {
    		
    		if(f.isDirectory())
	    		if(!confirm("Really recursively index directory:\n\t"+f.getAbsolutePath(),"Recursive Indexing Confirmation"))
	    			continue;
    		
    		try {
    			if(f.isFile())
    				this.index.index(f);
    			else if(f.isDirectory())
    				for(File f2 : getFiles(f))
    					this.index.index(f2);
    			
    		} catch(FileNotFoundException e) {
    			error("File Not Found: " + f.getName(),false,e);
    		} catch(IllegalArgumentException e) {
    			error("File Not Readable: " + f.getName(),false,e);
    		}
    	}
	}
	
	private void search(String query) {	
		if(query == null)
			return;
		
		this.collapseAll();
		
		int max_result_sz = 10;
		try {
			max_result_sz = Integer.parseInt(this.max_results.getText());
		} catch(NumberFormatException e) {
			;
		}
		
		long start = System.currentTimeMillis();
		SortedSet<QueryResult> result = this.index.query(query,max_result_sz);
		long time = System.currentTimeMillis() - start;
		
		this.root = new DefaultMutableTreeNode("Found " + result.size() + " result"+((result.size() != 1)?"s":"") +".");
		this.model.setRoot(root);
		
		this.messageBar.setText("Query completed in "+time+" ms.");
		
		if(result.size() == 0)
			return;
		
		DecimalFormat format = new DecimalFormat("0.00");
		for(QueryResult qr : result)
		{
			String sc = format.format(qr.similarity*100);	
			String filename = qr.file.getAbsolutePath(); 
				
			DefaultMutableTreeNode result_node = new DefaultMutableTreeNode("[sc: " + sc + "] " + qr.file.getName());
			this.root.add(result_node);
			
			result_node.add( new DefaultMutableTreeNode(filename) );
		}
		
		this.expandAll();
}	
	
	private void openFile() {
		String editor = getSystemTextEditor();
		DefaultMutableTreeNode selected = (DefaultMutableTreeNode)tree.getLastSelectedPathComponent();
		
		if(selected == null)
			return;
		
		String file = (String)selected.getUserObject();
		
		if(selected.getChildCount() != 0 || selected == root) {
			error("Error opening file",false,new Exception("Selected is not a file."));
			return;
		}
		
		try {
			if(editor != null)
				new ProcessBuilder(editor,file).start();
			else
				error("Error opening file",false,new Exception("Don't know your system's text editor process name"));
		} catch(IOException e) {
			error("Error opening file",true,e);
		}
	}
	
	private void showInfo() {
        this.info_frame.setVisible(true);
	}
	
	private void calculateMemoryUsage() {
		long free = Runtime.getRuntime().freeMemory();
		long total = Runtime.getRuntime().totalMemory();
		long used = total - free;
		
		int usage = (int)(((float)used/(float)total)*100);
		
		this.mem_usage.setValue(usage);
		this.mem_usage.setToolTipText("JVM Memory Usage: " + used + "/" + total);
	}
	
	private void error(String title,boolean stackTrace,Exception e) {
		JOptionPane.showMessageDialog(this,stackTrace?e.getStackTrace():e.getMessage(), title, JOptionPane.ERROR_MESSAGE);
	}
	
	private boolean confirm(String message,String title) {
		return JOptionPane.showConfirmDialog
				(this, message, title, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION;
	}
	
	private void collapseAll() {
		for(int i = 0; i < this.tree.getRowCount(); i++)
			this.tree.collapseRow(i);
	}
	
	private void expandAll() {
		for(int i = 0; i < this.tree.getRowCount(); i++)
			this.tree.expandRow(i);
	}
	
	public static List<File> getFiles(File root) {
		List<File> result = new Vector<File>();
		
		for(File f : root.listFiles(directoryFilter))
			result.addAll( getFiles(f) );
		
		for(File f : root.listFiles(fileFilter))
			result.add(f);
		
		return result;
	}
	
	public static String getSystemTextEditor() {
		String os = System.getProperty("os.name").toLowerCase();

		if(os.startsWith("windows")) {
			return "notepad";
		} else if(os.equals("linux")) {
			String desktop_session = System.getenv("DESKTOP_SESSION");
			if(desktop_session == null)
				return "vi";
			
			if(desktop_session.equals("kde"))
				return "kate";
			else if(desktop_session.equals("gnome"))
				return "gedit";
			
			return "xedit";
		} else if(os.equals("mac os")) {
			return "textedit";
		}
		
		return null;
	}	

	public static void main(String[] args) {
        try {
        	Plastic3DLookAndFeel lf = new Plastic3DLookAndFeel();
        	Plastic3DLookAndFeel.setPlasticTheme(new ExperienceBlue());
            UIManager.setLookAndFeel(lf);
        } catch (Exception e) {
            // PlasticXP (JGoodies) is not in the class path; ignore.
        }		
		
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {            	
        		InvertedIndex index = new InvertedIndex();
        		
                //Create and set up the frame
                IndexGUI frame = new IndexGUI(index);
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

                //Display the window.
                frame.setPreferredSize(new Dimension(800,600));
                frame.pack();
                frame.setVisible(true);
            }
        });		
	}	
}
