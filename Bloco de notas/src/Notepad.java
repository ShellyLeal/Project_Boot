import javax.swing.*; // inicia a biblioteca para interface gr�fica
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.Utilities;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import java.awt.*;
import java.awt.event.*; // para os eventos
import java.util.Calendar;
import java.util.Scanner; // para ler um arquivo
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.*; // para escrever e criar um arquivo

import java.text.SimpleDateFormat;

public class Notepad extends JFrame implements ActionListener {
    
	 int posInicial = 0;
	private MenuBar menuBar = new MenuBar(); // cria o menu
	private JEditorPane textPane = new JEditorPane();
	private Menu file = new Menu();
	private Menu file2 = new Menu();
	private Menu file3 = new Menu();
	private Document doc = textPane.getDocument();
	
	// Op��es do menu
	
	private MenuItem openFile = new MenuItem(); // op��o de abrir

	private MenuItem saveFile = new MenuItem(); // op��o de salvar

	private MenuItem close = new MenuItem(); // op��o de fechar

    private MenuItem search = new MenuItem();
 
    boolean isNeedCursorChange=true;
    boolean isInProgress=false;
    
	public Notepad() {
        super();
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // opera��o de fechar a janela
		this.textPane.setEditorKit(new MyHTMLEditorKit());
		this.textPane.setContentType("text/html");
		this.initListener();
		JScrollPane scroll = new JScrollPane(this.textPane){
            public void setCursor(Cursor cursor) {
                if (isNeedCursorChange) {
                    super.setCursor(cursor);
                }
            }
        };
        
        this.getContentPane().add(scroll);
        
        this.textPane.addHyperlinkListener(new HTMLListener());
        
		this.setSize(500, 300); // tamanho inicial da janela de texto

		this.setTitle("Bloco de Notas Java"); // t�tulo da janela

		 

		this.textPane.setFont(new Font("Century Gothic", Font.BOLD, 12)); // fonte
		
		this.getContentPane().setLayout(new BorderLayout());

		this.getContentPane().add(textPane);
        
		
		
		// inclus�o do menu de op��es

		this.setMenuBar(this.menuBar);

		this.menuBar.add(this.file);
		this.menuBar.add(this.file2);
		this.menuBar.add(this.file3);
		this.file.setLabel("Arquivo");
		this.file2.setLabel("Editar");


		this.openFile.setLabel("Abrir"); // nomeia a op��o de abrir
		this.openFile.addActionListener(this); // para detectar o clique
		this.openFile.setShortcut(new MenuShortcut(KeyEvent.VK_O, false)); // tecla de atalho
		this.file.add(this.openFile); // adiciona ao menu
		

		this.saveFile.setLabel("Salvar como..."); // nomeia a op��o de salvar
		this.saveFile.addActionListener(this); // para detectar o clique
		this.saveFile.setShortcut(new MenuShortcut(KeyEvent.VK_S, false)); // tecla de atalho																		
		this.file.add(this.saveFile); // adiciona ao menu

		this.close.setLabel("Fechar"); // nomeia a op��o de fechar
		this.close.addActionListener(this); // para detectar o clique
		this.close.setShortcut(new MenuShortcut(KeyEvent.VK_F4, false)); // atalho "CTRL+F4"
		this.file.add(this.close); // adiciona ao menu

	    this.search.setLabel("Localizar...");// nomeia a op��o de localizar
	    this.search.addActionListener(this);// para detectar o clique
		this.search.setShortcut(new MenuShortcut(KeyEvent.VK_F, false)); // tecla de atalho "CTRL+F"
	    this.file2.add(this.search);// adiciona ao menu
		
	}
    
    private void initListener() {
        textPane.getDocument().addDocumentListener(new DocumentListener(){
            public void insertUpdate(DocumentEvent event) {
                final DocumentEvent e=event;
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        if (e.getDocument() instanceof HTMLDocument
                                && e.getOffset()>0
                                && e.getLength()==1
                                && !isInProgress) {
                            try {

                                HTMLDocument doc=(HTMLDocument)e.getDocument();
                                String text=doc.getText(e.getOffset(), e.getLength());
                                if (text.charAt(0)==' ' || text.charAt(0)=='\n' || text.charAt(0)=='\t') {
                                    int start=Utilities.getWordStart(Notepad.this.textPane, e.getOffset()-1);
                                    text=doc.getText(start, e.getOffset()-start);
                                    if (text.startsWith("www")) {
                                        isInProgress=true;
                                        HTMLEditorKit kit=(HTMLEditorKit)textPane.getEditorKit();
                                        //the next 3 lines are necessary to create separate text elem
                                        //to be replaced with link
                                        SimpleAttributeSet a=new SimpleAttributeSet();
                                        a.addAttribute("DUMMY_ATTRIBUTE_NAME","DUMMY_ATTRIBUTE_VALUE");
                                        doc.setCharacterAttributes(start, text.length(), a, false);
 
                                        Element elem=doc.getCharacterElement(start);
                                        String html="<a href=\"http://" + text + "\">" + text + "</a>";
                                        doc.setOuterHTML(elem, html);
                                        isInProgress=false;
                                    }
                                }
                            } catch (BadLocationException e1) {
                                e1.printStackTrace();
                            } catch (IOException e1) {
                                e1.printStackTrace();
                            }
                        }
                    }
                });
            }
            public void removeUpdate(DocumentEvent e) {
            }
            public void changedUpdate(DocumentEvent e) {
            }
        });
    }
	
	private class HTMLListener implements HyperlinkListener {
        public void hyperlinkUpdate(HyperlinkEvent e) {
           	
          if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
              try {
            	  java.awt.Desktop.getDesktop().browse(e.getURL().toURI());
              } catch (Exception exc) {
                  exc.printStackTrace();
              }
          }
        }
      }
	
	
	public class MyHTMLEditorKit extends HTMLEditorKit {

        MyLinkController handler=new MyLinkController();
        public void install(JEditorPane c) {
            MouseListener[] oldMouseListeners=c.getMouseListeners();
            MouseMotionListener[] oldMouseMotionListeners=c.getMouseMotionListeners();
            super.install(c);
            //the following code removes link handler added by original
            //HTMLEditorKit

            for (MouseListener l: c.getMouseListeners()) {
                c.removeMouseListener(l);
            }
            for (MouseListener l: oldMouseListeners) {
                c.addMouseListener(l);
            }

            for (MouseMotionListener l: c.getMouseMotionListeners()) {
                c.removeMouseMotionListener(l);
            }
            for (MouseMotionListener l: oldMouseMotionListeners) {
                c.addMouseMotionListener(l);
            }
 
            //add out link handler instead of removed one
            c.addMouseListener(handler);
            c.addMouseMotionListener(handler);
        }
 
        public class MyLinkController extends LinkController {

            public void mouseClicked(MouseEvent e) {
                JEditorPane editor = (JEditorPane) e.getSource();
 
                if (editor.isEditable() && SwingUtilities.isLeftMouseButton(e)) {
                    if (e.getClickCount()==1) {
                        editor.setEditable(false);
                        super.mouseClicked(e);
                        editor.setEditable(true);
                    }
                }

            }
            public void mouseMoved(MouseEvent e) {
                JEditorPane editor = (JEditorPane) e.getSource();
 
                if (editor.isEditable()) {
                    isNeedCursorChange=false;
                    editor.setEditable(false);
                    isNeedCursorChange=true;
                    super.mouseMoved(e);
                    isNeedCursorChange=false;
                    editor.setEditable(true);
                    isNeedCursorChange=true;
                }
            }

        }
    }
	
	
	public void actionPerformed(ActionEvent e) {

		// Evento fechar arquivo

		if (e.getSource() == this.close)

			this.dispose(); // fecha o aplicativo



		// Evento abrir arquivo

		else if (e.getSource() == this.openFile) {

			JFileChooser open = new JFileChooser(); // abre o di�logo para escolher o arquivo
							
           
			int option = open.showOpenDialog(this); // acrescenta a op��o 'aceitar' ou 'cancelar'
													
			if (option == JFileChooser.APPROVE_OPTION) {

				this.textPane.setText(""); // limpa a �rea de texto para depois incluir o conte�do do arquivo aberto

				try {
                  /* BufferedReader in = new BufferedReader(new FileReader(open.getSelectedFile()));
                   String line = in.readLine();
                   while(line!= null){
                	   this.doc.insertString(doc.getLength(),line+"\n",null);
                	   line = in.readLine();
                	   System.out.print(line);
                   }*/
					// scan: ler o arquivo

					Scanner scan = new Scanner(new FileReader(open.getSelectedFile().getPath()));
					while (scan.hasNext()){ // funciona at� ler o final do arquivo
						System.out.print(scan.nextLine() + "\n");
					   this.doc.insertString(doc.getLength(), scan.nextLine() + "\n", null); // adiciona uma linha ao arquivo
					   
					}System.out.print("sai");
				} catch (Exception ex) { // gerar mensagens para as exce��es

					System.out.println(ex.getMessage());

				}

			}

			//antigo
			
			

			
													
			/*if (option == JFileChooser.APPROVE_OPTION) {

				this.textPane.setText(""); // limpa a �rea de texto e inclui o conte�do do arquivo aberto

				try {

					// scan: ler o arquivo

					Scanner scan = new Scanner(new FileReader(open.getSelectedFile().getPath()));

					while (scan.hasNext()) // funciona at� ler o final do arquivo
					
						this.doc.insertString(doc.getLength(), scan.nextLine() + "\n", null); // adiciona uma linha ao arquivo
			
				} catch (Exception ex) { // gerar mensagens para as exce��es

					System.out.println(ex.getMessage());

				}

			}*/
			
			
			
		}
		
		// Evento salvar arquivo

		else if (e.getSource() == this.saveFile) {

			JFileChooser save = new JFileChooser(); // abre o di�logo para escolher arquivo
			int option = save.showSaveDialog(this);
            
			if (option == JFileChooser.APPROVE_OPTION) {

				try {
                    
					// out: criar arquivo para salvar o texto e hor�rio de modifica��o
					BufferedWriter out = new BufferedWriter(new FileWriter(save.getSelectedFile().getPath() + ".txt"));
				    System.out.println(save.getSelectedFile().getAbsolutePath()+ ".txt");
				    
				    
					Calendar cal = Calendar.getInstance();
					SimpleDateFormat sdf = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss");

					out.write(this.textPane.getText() + "\r\n�ltima modifica��o: " + sdf.format(cal.getTime())); // passa o conteudo da area de texto para o arquivo
																												
					out.close(); // fecha o arquivo

				} catch (Exception ex) { // gera mensagens para exce��es

					System.out.println(ex.getMessage());

				}

			}

		}
		else if (e.getSource() == this.search){		
			
			 JPanel fields = new JPanel(new GridBagLayout());
			 fields.setLayout(new FlowLayout());
		    JTextField texto = new JTextField(10);
		    JButton btn = new JButton("Localizar/Localizar Pr�ximo");
		    JButton cancel = new JButton("Cancelar");
		    add(fields, BorderLayout.NORTH);
		    add(new JScrollPane(textPane));
		    fields.add(texto);
		    fields.add(btn);
		    fields.add(cancel);
		    setVisible(true);
		    btn.addActionListener(
		        new ActionListener(){
		            public void actionPerformed(ActionEvent e){
		            	DefaultStyledDocument document = (DefaultStyledDocument) textPane.getDocument();
		            	
		               try {
						String contText = document.getText(0, document.getLength());
					
		            	
		               String pesquisa = texto.getText();
		               int res = contText.indexOf(pesquisa, posInicial);
		               if(res < 0){
		                  JOptionPane.showMessageDialog(null, "Texto n�o encontrado");
		                  posInicial = 0;   
		               } 
		               else{
		                  textPane.requestFocus();
		                  textPane.select(res, res + pesquisa.length());
		                  posInicial = res + pesquisa.length();
		               }
		               } catch (BadLocationException ex) {
							Logger.getLogger(JTextPane.class.getName()).log(Level.SEVERE, null, ex);
						}
		            }
		        }   
		    );
		    cancel.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e){fields.setVisible(false);}});
		

		       
		   
		  }
		  
		  
		}
		
	// m�todo main

	public static void main(String args[]) {

		Notepad app = new Notepad();

		app.setVisible(true);

	}

}
