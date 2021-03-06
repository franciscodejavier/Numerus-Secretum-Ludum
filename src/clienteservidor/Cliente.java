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

import matematicas.Consule;
import matematicas.Fondo;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

public class Cliente extends JFrame implements WindowListener, MouseListener, KeyListener {
    private static final long serialVersionUID = 1;
    
    JTextField txtNumeroCliente = new JTextField();
    JButton btnOk = new JButton("Ok");
    Fondo fondo = new Fondo();
    JScrollPane scrollPane = new JScrollPane(22, 32);
    JPanel panel = new JPanel();
    JLabel[] labels = new JLabel[1000];
    int contadorLineas = 0;
    int contador = 0;
    Border raise = BorderFactory.createEtchedBorder(0);
    static String respuesta = "espera";
    GridBagConstraints gbc = new GridBagConstraints();
    JPasswordField txtNumeroSecreto = new JPasswordField();
    JButton btnOkk = new JButton("Encrypt");
    JTextField txtIP = new JTextField("127.0.0.1");
    JButton btnIP = new JButton("TCP/IP");
    Socket miServicio;
    ServerSocket socketServicio;
    private Socket socketCliente;
    OutputStream outputStream;
    InputStream inputStream;
    DataOutputStream SalidaDatos;
    DataInputStream entradaDatos;
    static String numeroSecretoCliente = "";
    static String numeroServidor = "";
    static String numeroCliente = "";
    static boolean existsNumeroSecretoServidor = false;
    static boolean opcion = true;
    static String ip = "";
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

    public Cliente() {
    	// Hilo para el botón
        hiloBtnOkk = new Thread(new Runnable(){
            @SuppressWarnings("deprecation")
			@Override
            public void run() {
                numeroSecretoCliente = txtNumeroSecreto.getText();
                if (numeroSecretoCliente.length() != 4) {
                    nuevaLinea("[ERROR] 4 d\u00edgitos", "RED");
                } else {
                    Creacion2();
                    nuevaLinea("[!] Cifrando n\u00famero secreto", "WHITE");
                    nuevaLinea("[!] N\u00famero secreto cifrado", "GREEN");
                }
            }
        });
        
        // Arquitectura gráfica
        setLayout(null);
        setTitle("Arcanum Cliente");
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
            	IP();
                btnIP.addActionListener(event -> {BtnIP();});
                btnOkk.addActionListener(event -> {hiloBtnOkk.start();});
                btnOk.addActionListener(event -> {try {BtnOK();} 
                catch (Exception e) {e.printStackTrace();
				}});
            }
        });
        hiloInicio.start();
        
        // Listeners
        addWindowListener(this);
        addMouseListener(this);
        addKeyListener(this);
        txtNumeroCliente.addKeyListener(this);
    }

    public static void main(String[] args) throws NumberFormatException, IOException, ClassNotFoundException, SQLException {new clienteservidor.Cliente();}

    public void mouseClicked(MouseEvent me) {}
    public void windowActivated(WindowEvent arg0) {}
    public void windowClosed(WindowEvent arg0) {}
    public void windowClosing(WindowEvent arg0) {System.exit(0);}
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

    // Clase para añadir una nueva línea a la pantalla del PC
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

    // Clase para enviar datos al servidor
    public void enviarDatos(String datos) throws Exception {
        try {
        	cifrado = encrypt(datos, key);
            outputStream = socketCliente.getOutputStream();
            SalidaDatos = new DataOutputStream(outputStream);
            SalidaDatos.writeUTF(cifrado);
            SalidaDatos.flush();
        }
        catch (IOException ex) {Logger.getLogger(Servidor.class.getName()).log(Level.SEVERE, null, ex);}
    }

    // Clase para escuchar datos provenientes del servidor
    public void escucharDatos(Socket socket) throws Exception {
        try {
            inputStream = socket.getInputStream();
            entradaDatos = new DataInputStream(inputStream);
            
            String descifrar = entradaDatos.readUTF();
            numeroServidor = decrypt(descifrar, key);
            //numeroServidor = entradaDatos.readUTF();
            
            boolean esNumero = true;
            try {Integer.parseInt(numeroServidor);}
            catch (Exception e) {esNumero = false;}
            if (esNumero) {
                respuesta = Consule.Consulta(numeroSecretoCliente, numeroServidor);
                nuevaLinea("<< " + numeroServidor + " --> " + respuesta, "GRAY");
                enviarDatos(respuesta);
            } 
            else {
                ++contador;
                nuevaLinea(">>" + numeroCliente + " --> " + numeroServidor, "WHITE");
                if (numeroServidor.equals("exploited")) {
                    Perdedor();
                    nuevaLinea("[ERROR] Error en sistema", "RED");
                    nuevaLinea("[ERROR] System HACKED", "RED");
                    nuevaLinea("[hacked] Has sido hackeado por server", "RED");
                    nuevaLinea("[!] Perdiendo control de equipo...", "WHITE");
                    nuevaLinea("server@system# Lo siento", "RED");
                    cerrarTodo();
                    new HalldelaFama();
                } 
                else if (numeroServidor.equals("mmmm")) {
                    Ganador();
                    enviarDatos("exploited");
                    nuevaLinea("[hacked] Has hackeado al servidor", "GREEN");
                    nuevaLinea("[!] Intentos realizados " + contador, "WHITE");
                    nuevaLinea("server@system# Enhorabuena", "GREEN");
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
        txtNumeroCliente.setEditable(false);
        txtNumeroCliente.setBorder(raise);
        txtNumeroCliente.setBorder(BorderFactory.createMatteBorder(3, 3, 3, 3, Color.GREEN));
        btnOk.setBorder(raise);
        btnOk.setBorder(BorderFactory.createMatteBorder(3, 3, 3, 3, Color.GREEN));
        btnOk.setText("Salir");
        btnOk.addActionListener(newEvent -> {setVisible(false);});
    }

    // Clase a ejecutar al perder
    public void Perdedor() {
        txtNumeroCliente.setEditable(false);
        txtNumeroCliente.setBorder(raise);
        txtNumeroCliente.setBorder(BorderFactory.createMatteBorder(3, 3, 3, 3, Color.RED));
        btnOk.setBorder(raise);
        btnOk.setBorder(BorderFactory.createMatteBorder(3, 3, 3, 3, Color.RED));
        btnOk.setText("Salir");
        btnOk.addActionListener(newEvent -> {setVisible(false);});
    }

    // Clase para cerrar todo
    public void cerrarTodo() {
        try {
            opcion = false;
            SalidaDatos.close();
            entradaDatos.close();
            socketCliente.close();
        }
        catch (IOException ex) {Logger.getLogger(Cliente.class.getName()).log(Level.SEVERE, null, ex);}
    }

    // Clase de encendido de máquina
    public void Encendido() {
        nuevaLinea("[!] Iniciando sistema...", "WHITE");
        nuevaLinea("[!] Sistema iniciado", "WHITE");
        nuevaLinea("[!] Aplicando reglas de firewall", "WHITE");
        nuevaLinea("[!] Hardening sistema...", "WHITE");
        nuevaLinea("[!] Sistema iniciado correctamente", "WHITE");
        nuevaLinea("[process] Requerida IP de v\u00edctima", "GREEN");
    }

    // Clase para almacenar la IP del servidor
    public void IP() {
        add(txtIP);
        txtIP.setBounds(170, 400, 200, 40);
        txtIP.setBackground(Color.GREEN);
        txtIP.setForeground(Color.WHITE);
        add(btnIP);
        btnIP.setBounds(380, 400, 90, 40);
        btnIP.setBackground(Color.GREEN);
        btnIP.setForeground(Color.WHITE);
        txtIP.setBorder(raise);
        txtIP.setBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, Color.WHITE));
        btnIP.setBorder(raise);
        btnIP.setBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, Color.WHITE));
    }

    // Clase para la primera creación
    public void Creacion() {
        remove(btnIP);
        remove(txtIP);
        add(txtNumeroSecreto);
        txtNumeroSecreto.setBounds(170, 400, 200, 40);
        txtNumeroSecreto.setBackground(Color.RED);
        txtNumeroSecreto.setForeground(Color.WHITE);
        add(btnOkk);
        btnOkk.setBounds(380, 400, 90, 40);
        btnOkk.setBackground(Color.RED);
        btnOkk.setForeground(Color.WHITE);
        txtNumeroCliente.setBorder(raise);
        txtNumeroCliente.setBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, Color.WHITE));
        btnOk.setBorder(raise);
        btnOk.setBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, Color.WHITE));
    }
    
    // Clase para la segunda creación
    public void Creacion2() {
        remove(btnOkk);
        remove(txtNumeroSecreto);
        add(txtNumeroCliente);
        txtNumeroCliente.setBounds(170, 400, 200, 40);
        txtNumeroCliente.setBackground(Color.BLACK);
        txtNumeroCliente.setForeground(Color.WHITE);
        add(btnOk);
        btnOk.setBounds(380, 400, 90, 40);
        btnOk.setBackground(Color.BLACK);
        btnOk.setForeground(Color.WHITE);
    }

    // Clase para el botón de la IP
    public void BtnIP() {
        ip = txtIP.getText();
        try {
            socketCliente = new Socket(ip, 5555);
            Thread hilo1 = new Thread(new Runnable(){

                @Override
                public void run() {
                    while (Cliente.opcion) {
                        try {escucharDatos(socketCliente);} 
                        catch (Exception e) {e.printStackTrace();}
                    }
                }
            });
            hilo1.start();
        }
        catch (Exception ex) {nuevaLinea(ex.getMessage(), "RED");}
        Creacion();
        nuevaLinea("[process] Target IP Preparado", "WHITE");
        nuevaLinea("[process] Introduzca secreto", "BLUE");
    }

    // Clase para el botón
    public void BtnOK() throws Exception {
        numeroCliente = txtNumeroCliente.getText();
        if (numeroCliente.length() != 4) {nuevaLinea("[ERROR] 4 d\u00edgitos", "RED");} 
        else {enviarDatos(numeroCliente);}
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

			JOptionPane.showMessageDialog(null, "Tu archivo ha sido subido satisfactoriamente al Hall de la Fama");
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
		f.setDialogTitle("¡Selecciona un archivo a subir al Hall de la Fama!");

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

