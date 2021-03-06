package clienteservidor;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.Border;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import matematicas.Consule;
import matematicas.Fondo;

public class Servidor extends JFrame implements WindowListener, MouseListener, KeyListener {
    private static final long serialVersionUID = 1;
    
    JTextField txtNumeroServidor = new JTextField();
    JButton btnOk = new JButton("Ok"); 					
    Fondo fondo = new Fondo();							
    Border raise = BorderFactory.createEtchedBorder(0);
    JScrollPane scrollPane = new JScrollPane(22, 32);
    JPanel panel = new JPanel();
    JLabel[] labels = new JLabel[1000];
    int contadorLineas = 0;
    int contador = 0;
    static String respuesta = "espera";
    GridBagConstraints gbc = new GridBagConstraints();
    JPasswordField txtNumeroSecreto = new JPasswordField();
    JButton btnOkk = new JButton("Encrypt");
    Socket miServicio;
    ServerSocket socketServicio;
    OutputStream outputStream;
    InputStream inputStream;
    DataOutputStream salidaDatos;
    DataInputStream entradaDatos;
    static String numeroSecretoServidor = "";
    static String numeroServidor = "";
    static String numeroCliente = "";
    static boolean existsNumeroSecretoCliente = false;
    private boolean opcion = true;
    Thread hiloBtnOkk;
    
    // key
 	static String texto, cifrado, eco;
 	static SecretKey secretKey;
    static String key = "Bar12345Bar12345";

    // ftp
    static FTPClient client = new FTPClient();
    static String servidorftp = "192.168.1.108";
    static String usuarioftp = "secreto";
    static String passftp = "b6qeyuge";
	static String direcInicial = "/";
	static String direcSelec = direcInicial;
	static String ficheroSelec = "";
    
    public Servidor() {
    	// Hilo para el botón
    	hiloBtnOkk = new Thread(new Runnable(){
            @SuppressWarnings("deprecation")
			@Override
            public void run() {
                numeroSecretoServidor = txtNumeroSecreto.getText();
                if (numeroSecretoServidor.length() != 4) {
                    nuevaLinea("[ERROR] 4 d\u00edgitos", "RED");
                } 
                else {
                    Creacion2();
                    nuevaLinea("[!] Cifrando n\u00famero secreto", "WHITE");
                    nuevaLinea("[!] N\u00famero secreto cifrado", "GREEN");
                }
            }
        });
        
    	// Arquitectura gráfica
        setLayout(null);
        setTitle("Arcanum Servidor");
        setLocationRelativeTo(null);
        setSize(639, 480);
        setVisible(true);
        setResizable(false);
        add(fondo);
        fondo.setLayout(null);
        fondo.setBackground("img/monitor.jpg");
        fondo.setVisible(true);
        fondo.setOpaque(false);
        fondo.setBounds(0, 0, 639, 480);
        getContentPane().add(panel);
        fondo.add(scrollPane);
        scrollPane.setBounds(156, 57, 337, 290);
        panel.setBackground(new Color(1, 1, 1, 250));
        panel.setLayout(new GridBagLayout());
        gbc.insets = new Insets(1, 1, 1, 300);
        gbc.anchor = 18;
      
        // Hilo de inicio
        Thread hiloInicio = new Thread(new Runnable(){
            @Override
            public void run() {
                Encendido();
                Creacion();
                btnOkk.addActionListener(event -> {hiloBtnOkk.start();});
                btnOk.addActionListener(event -> {
                	try {BtnOk();} 
                	catch (Exception e) {e.printStackTrace();}});
                try {
                    socketServicio = new ServerSocket(5555);
                    nuevaLinea("Servicio en escucha en puerto: 5555", "BLUE");
                    miServicio = socketServicio.accept();
                    
                    // Subhilo de recepción de datos
                    Thread hilo = new Thread(new Runnable(){
                        @Override
                        public void run() {
                            while (opcion) {
                                try {
									recibirDatos();
								} catch (Exception e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
                            }
                        }
                    });
                    hilo.start();
                }
                catch (Exception ex) {nuevaLinea("Error al abrir los sockets", "RED");}
            }
        });
        hiloInicio.start();
        
        // Listeners
        addWindowListener(this);
        addMouseListener(this);
        addKeyListener(this);
        txtNumeroServidor.addKeyListener(this);
    }

    public static void main(String[] args) throws NumberFormatException, IOException, ClassNotFoundException, SQLException {new Servidor();}

    public void mouseClicked(MouseEvent me) {}
    public void windowActivated(WindowEvent arg0) {}
    public void windowClosed(WindowEvent arg0) {}

    public void windowClosing(WindowEvent arg0) {
        System.exit(0);
        try {miServicio.close();}
        catch (IOException e) {e.printStackTrace();}
    }

    public void windowDeactivated(WindowEvent arg0) {}
    public void windowDeiconified(WindowEvent arg0) {}
    public void windowIconified(WindowEvent arg0) {}
    public void windowOpened(WindowEvent arg0) {}
    public void mouseEntered(MouseEvent arg0) {}
    public void mouseExited(MouseEvent arg0) {}
    public void mousePressed(MouseEvent arg0) {}
    public void mouseReleased(MouseEvent arg0) {}
   
    public void keyPressed(KeyEvent ke) {
        // Si pulsamos intro sobre el textfield, será igual que presionar sobre el botón
    	if (ke.getKeyCode() == 10) {btnOk.doClick();}
    }
    
    public void keyReleased(KeyEvent ke) {}
    public void keyTyped(KeyEvent ke) {}
    
    // Método para añadir una nueva línea a la pantalla del PC
    public void nuevaLinea(String texto, String color) {
        labels[contadorLineas] = new JLabel(texto);
        if (color.equals("RED")) {labels[contadorLineas].setForeground(Color.RED);} 
        else if (color.equals("WHITE")) {labels[contadorLineas].setForeground(Color.WHITE);} 
        else if (color.equals("GREEN")) {labels[contadorLineas].setForeground(Color.GREEN);} 
        else if (color.equals("GRAY")) {labels[contadorLineas].setForeground(Color.GRAY);} 
        else if (color.equals("BLUE")) {labels[contadorLineas].setForeground(Color.BLUE);} 
        else {labels[contadorLineas].setForeground(Color.WHITE);}
        
        gbc.gridx = 0;
        gbc.gridy = contadorLineas;
        panel.add((Component)labels[contadorLineas], gbc);
        scrollPane.setViewportView(panel);
        ++contadorLineas;
        
        try {Thread.sleep(500);}
        catch (Exception exception) {}
        scrollPane.getVerticalScrollBar().setValue(scrollPane.getVerticalScrollBar().getMaximum());
    }

    // Método para enviar datos al cliente
    public void enviarDatos(String datos) throws Exception {
        try {
        	cifrado = encrypt(datos, key);
        	outputStream = miServicio.getOutputStream();
        	salidaDatos = new DataOutputStream(outputStream);
        	salidaDatos.writeUTF(cifrado);
        	salidaDatos.flush();
        }
        catch (IOException ex) {Logger.getLogger(Servidor.class.getName()).log(Level.SEVERE, null, ex);}
    }

    // Método de recepción de datos
    public void recibirDatos() throws Exception {
        try {
            inputStream = miServicio.getInputStream();
            entradaDatos = new DataInputStream(inputStream);
            String descifrar = entradaDatos.readUTF();
            String numeroCliente = decrypt(descifrar, key);
            //String numeroCliente = entradaDatos.readUTF();
            boolean esNumero = true;
           
            try {Integer.parseInt(numeroCliente);}
            catch (Exception e) {esNumero = false;}
            
            if (esNumero) {
                respuesta = Consule.Consulta(numeroSecretoServidor, numeroCliente);
                nuevaLinea("<< " + numeroCliente + " --> " + respuesta, "GRAY");
                enviarDatos(respuesta);
            } 
            else {
                ++contador;
                nuevaLinea(">>" + numeroServidor + " --> " + numeroCliente, "WHITE");
                if (numeroCliente.equals("exploited")) {
                    Perdedor();
                    nuevaLinea("[ERROR] Error en sistema", "RED");
                    nuevaLinea("[ERROR] System HACKED", "RED");
                    nuevaLinea("[hacked] Has sido hackeado por cliente", "RED");
                    nuevaLinea("[!] Perdiendo control del servidor...", "WHITE");
                    nuevaLinea("cliente@system# Lo siento", "RED");
                    cerrarTodo();
                    new HalldelaFama();
                } 
                else if (numeroCliente.equals("mmmm")) {
                    Ganador();
                    enviarDatos("exploited");
                    nuevaLinea("[hacked] Has hackeado al cliente", "GREEN");
                    nuevaLinea("[!] Intentos realizados " + contador, "WHITE");
                    nuevaLinea("cliente@system# Enhorabuena", "GREEN");
                    // CODIGO FTP
                	connftp();
                	subirftp();
                	cerrarftp();
                    cerrarTodo();
                    new HalldelaFama();
                }
            }
        }
        catch (IOException ex) {Logger.getLogger(Servidor.class.getName()).log(Level.SEVERE, null, ex);}
    }

    // Clase a ejecutar al ganar
    public void Ganador() {
        txtNumeroServidor.setEditable(false);
        txtNumeroServidor.setBorder(raise);
        txtNumeroServidor.setBorder(BorderFactory.createMatteBorder(3, 3, 3, 3, Color.GREEN));
        btnOk.setBorder(raise);
        btnOk.setBorder(BorderFactory.createMatteBorder(3, 3, 3, 3, Color.GREEN));
        btnOk.setText("Salir");
        btnOk.addActionListener(newEvent -> {setVisible(false);});
    }

    // Clase a ejecutar al perder
    public void Perdedor() {
        txtNumeroServidor.setEditable(false);
        txtNumeroServidor.setBorder(raise);
        txtNumeroServidor.setBorder(BorderFactory.createMatteBorder(3, 3, 3, 3, Color.RED));
        btnOk.setBorder(raise);
        btnOk.setBorder(BorderFactory.createMatteBorder(3, 3, 3, 3, Color.RED));
        btnOk.setText("Salir");
        btnOk.addActionListener(newEvent -> {setVisible(false);});
    }

    // Clase para cerrar todo
    public void cerrarTodo() {
        try {
            opcion = false;
            salidaDatos.close();
            entradaDatos.close();
            socketServicio.close();
            miServicio.close();
        }
        catch (IOException ex) {Logger.getLogger(Servidor.class.getName()).log(Level.SEVERE, null, ex);}
    }

    // Clase de encendido de máquina
    public void Encendido() {
        nuevaLinea("[!] Iniciando sistema...", "WHITE");
        nuevaLinea("[!] Sistema iniciado", "WHITE");
        nuevaLinea("[!] Aplicando reglas de firewall", "WHITE");
        nuevaLinea("[!] Hardening sistema...", "WHITE");
        nuevaLinea("[!] Sistema iniciado correctamente", "WHITE");
        nuevaLinea("[process] Introduzca el secreto", "BLUE");
    }

    // Clase de primera creación
    public void Creacion() {
        add(txtNumeroSecreto);
        txtNumeroSecreto.setBounds(170, 400, 200, 40);
        txtNumeroSecreto.setBackground(Color.RED);
        txtNumeroSecreto.setForeground(Color.WHITE);
        add(btnOkk);
        btnOkk.setBounds(380, 400, 90, 40);
        btnOkk.setBackground(Color.RED);
        btnOkk.setForeground(Color.WHITE);
        txtNumeroServidor.setBorder(raise);
        txtNumeroServidor.setBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, Color.WHITE));
        btnOk.setBorder(raise);
        btnOk.setBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, Color.WHITE));
    }

    // Clase de segunda creación
    public void Creacion2() {
        remove(btnOkk);
        remove(txtNumeroSecreto);
        add(txtNumeroServidor);
        txtNumeroServidor.setBounds(170, 400, 200, 40);
        txtNumeroServidor.setBackground(Color.BLACK);
        txtNumeroServidor.setForeground(Color.WHITE);
        add(btnOk);
        btnOk.setBounds(380, 400, 90, 40);
        btnOk.setBackground(Color.BLACK);
        btnOk.setForeground(Color.WHITE);
    }

    // Clase de botón
    public void BtnOk() throws Exception {
        numeroServidor = txtNumeroServidor.getText();
        if (numeroServidor.length() != 4) {nuevaLinea("[ERROR] 4 d\u00edgitos", "RED");} 
        else {enviarDatos(numeroServidor);}
    }
    
    // Clase para cifrar
    public static String encrypt(String strClearText, String strKey) throws Exception {
		SecretKeySpec skeyspec = new SecretKeySpec(strKey.getBytes(), "AES");
		Cipher cipher = Cipher.getInstance("AES");
		cipher.init(Cipher.ENCRYPT_MODE, skeyspec);
		byte[] encrypted = cipher.doFinal(strClearText.getBytes());
		Base64.Encoder encoder = Base64.getEncoder();
		String encryptedString = encoder.encodeToString(encrypted);
		return encryptedString;
	}
    
    // Clase para cifrado
    public static SecretKey getSecretEncryptionKey() throws Exception {
		KeyGenerator generator = KeyGenerator.getInstance("AES");
		generator.init(128); // The AES key size in number of bits
		SecretKey secKey = generator.generateKey();

		return secKey;
	}
    
    // Clase para descifrado
    public static String decrypt(String strEncrypted, String strKey) throws Exception {
		String decrypted = "";
		SecretKeySpec skeyspec = new SecretKeySpec(strKey.getBytes(), "AES");
		Base64.Decoder decoder = Base64.getDecoder();
		Cipher cipher = Cipher.getInstance("AES");
		cipher.init(Cipher.DECRYPT_MODE, skeyspec);
		decrypted = new String(cipher.doFinal(decoder.decode(strEncrypted)));

		return decrypted;
	}
    
    public static void connftp() {
    	try {
    		  client.connect(servidorftp);
    		  client.login(usuarioftp,passftp);
    	} 
    	catch (IOException ioe) {}
    }
            
    @SuppressWarnings("unused")
	private boolean subirfichero(String archivo, String soloNombre) throws IOException {
		client.setFileType(FTP.BINARY_FILE_TYPE);
		BufferedInputStream in = new BufferedInputStream(new FileInputStream(archivo));
		boolean ok = false;

		client.changeWorkingDirectory(direcSelec);
		if (client.storeFile(soloNombre, in)) {

			JOptionPane.showMessageDialog(null, "Enhorabuena, campeón. Tu reliquia ha sido subida.");
			FTPFile[] ff2 = null;
			ff2 = client.listFiles();
			ok = true;
		} 
		return ok;
	}

    public void subirftp() {
    	JFileChooser f;
		File file;
		f = new JFileChooser();
		f.setFileSelectionMode(JFileChooser.FILES_ONLY);
		f.setDialogTitle("¡Selecciona un archivo a subir, campéon!");

		int returnVal = f.showDialog(f, "Cargar");
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			file = f.getSelectedFile();
			String archivo = file.getAbsolutePath();
			String nombreArchivo = file.getName();
			try {subirfichero(archivo, nombreArchivo);} 
			catch (IOException e1) {e1.printStackTrace();}
		}
    }
    
    public void cerrarftp() throws IOException {
    	client.logout();
    	client.disconnect();
    }
}

