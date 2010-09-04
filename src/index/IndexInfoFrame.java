package index;

import generic.Tuple2;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.Timer;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

/**
 * A Frame to visualize the current status of the document index as well as the indexing
 * threads.  Unfortunately, because of threading (I'm assuming), the indexing thread
 * visualization does not seem to work.
 * @author William Howard
 */
public class IndexInfoFrame extends JFrame implements IndexListener, ActionListener {
	public static final long serialVersionUID = 0x00;
	private static String idle_str = "idle";
	
	private InvertedIndex index = null;
	
	private Timer timer = null;
	
	private JLabel DocSz = new JLabel("0");
	private JLabel TermSz = new JLabel("0");
	private JLabel QueueSz = new JLabel("0");
	
	private JPanel threadPane = new JPanel();
	private JScrollPane jsp = new JScrollPane(threadPane);
	
	private JProgressBar[] thread = null;
	private JLabel[] threadfile = null;
	
	public IndexInfoFrame(InvertedIndex index) {
		super("Inverted Index Information:");
		
		this.index = index;
		this.index.addIndexListener(this);
		
		this.jsp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		this.jsp.setPreferredSize(new Dimension(400,300));
		
		int thread_sz = this.index.getNumberThreads();
		this.thread = new JProgressBar[thread_sz];
		this.threadfile = new JLabel[thread_sz];
		
		for(int i = 0; i < thread_sz; i++) {
			this.thread[i] = new JProgressBar(0,100);
			this.threadfile[i] = new JLabel(idle_str);
		}
		
		this.timer = new Timer(10,this);
		this.timer.start();
			
		this.add(this.buildPanel());
	}
	
	private JPanel buildPanel() {
		FormLayout layout = new FormLayout("right:max(60dlu;pref), 3dlu, 50dlu:grow");
		
        DefaultFormBuilder builder = new DefaultFormBuilder(layout);
        builder.setDefaultDialogBorder();
        
        builder.appendSeparator("Index Size");
        builder.append("Document Collection:",this.DocSz);
        builder.append("Global Terms:",this.TermSz);
        builder.append("Files in Queue:",this.QueueSz);
        builder.appendSeparator("Index Threads");
        builder.append(this.jsp, 3);
        
        FormLayout layout2 = new FormLayout("right:max(5dlu;pref), 3dlu, 105dlu:grow");
        DefaultFormBuilder builder2 = new DefaultFormBuilder(layout2);
        
        for(int i = 0; i < this.thread.length; i++) {
        	builder2.append(i + ":",this.thread[i]);
        	builder2.append("",this.threadfile[i]);
        }
        this.threadPane.add(builder2.getPanel());
        
        return builder.getPanel();
	}
	
	public void actionPerformed(ActionEvent ev) {
		Map<Integer,Tuple2<Float,File>> tmap = this.index.getThreadMap();
		
		for(Map.Entry<Integer, Tuple2<Float,File>> e : tmap.entrySet()) {
			int id = e.getKey();
			
			int progress = (int)Math.floor(e.getValue().first*100);
			
			File file = e.getValue().second;
			String fname = (file==null)? idle_str : file.getName();
			
			this.thread[id].setValue(progress);
			this.threadfile[id].setText(fname);
		}
	}
	
	public void sizeChanged(szTypes item, int new_size) {
		if(item.equals(IndexListener.szTypes.DOCUMENT_COLLECTION))
			this.DocSz.setText("" + new_size);
		else if(item.equals(IndexListener.szTypes.FILE_QUEUE))
			this.QueueSz.setText("" + new_size);
		else if(item.equals(IndexListener.szTypes.TERM_COLLECTION))
			this.TermSz.setText("" + new_size);
	}
}
