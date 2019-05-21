/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package map;

import characters.Jugador;
import data_level.DatosNivel;
import exception_serialization.*;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import menu.*;
import imagen.*;
import location.*;
import org.newdawn.slick.*;
import org.newdawn.slick.state.*;
import org.newdawn.slick.tiled.*;

/**
 *
 * @author Senapi Aroal
 * @author Davidcawork
 */
public class Nivel2 extends BasicGameState{

    //Arraylist donde guardamos todas las escenas de ese nivel
    private ArrayList<Escena> escenas = new ArrayList<>();
    
    //Variable para obtener las funciones del teclado
    private Input entrada;
    
    //Variable que representa al jugador
    private Jugador j; 
    
    //Variable que indica cuantas escenas tiene este nivel
    private final int numEscenas = 3;  
    
    //Cantidad de objetos que tendrá cada escena
    private final int[] numObjetos = {1,0,0}; 
    
    //Cantidad de enemigos que tendrá cada escena
    private final int[] numEnemigos = {4,5,3};
    
    //Cantidad de cofres que tendrá cada escena
    private final int[] numCofres = {1,4,3};
    
    //Variable para extraer toda la información acerca del nivel especificado
    private DatosNivel datos  = new DatosNivel(numEscenas,numObjetos,numEnemigos,numCofres);

    //Reloj para controlar movimiento
    private int reloj;
    
    private MenuPauseGame menu;
    
    private boolean area1,area2,area3;
    
    private Sprite area1S,area2S,area3S;
    
    private Music musica;
    
    private AlmacenarAvatar almacenar = new AlmacenarAvatar();
    /**
     * Constructor de la clase Nivel1
     * 
     * @param nombre
     */
    public Nivel2(String nombre){
        datos.datosNivel2();
        j = almacenar.cargarDatos(2).get(nombre).devolverJugador(datos);
        for(int i = 0;i<numEscenas;i++){
            Escena es;
            try {
                es = new Escena(new TiledMap("./res/mapas/Nivel2/layout_tmx/escena_"+(i+1)+".tmx","./res/mapas/Nivel2/resources_tsx"),datos.mapasNivel(i),datos.objetosNivel(i),datos.entradasNivel(i),datos.salidasNivel(i),datos.enemigosNivel(i),datos.cofresNivel(i));
                escenas.add(es);
            } catch (SlickException ex) {
                Logger.getLogger(Nivel1.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @Override
    public void init(GameContainer container, StateBasedGame game) throws SlickException {
        reloj = 0;
        area1 = true;
        area1S = new Sprite("./res/mapas/resource_png/fondo_negro_1024x192.png");
        area2 = false;
        area2S = new Sprite("./res/mapas/resource_png/fondo_negro_1024x256.png",new Punto(0,192));
        area3 = true;
        area3S = new Sprite("./res/mapas/resource_png/fondo_negro_1024x320.png",new Punto(0,448));
        menu = new MenuPauseGame(container);
        /*  Daño fijado a 50 en el primer nivel this.getNivelJugador()*50 */
        j.getHud().iniciarJugador(j.getHp());
        entrada = container.getInput(); 
        musica = new Music("./res/audio/music/musicaNivel2.ogg");
        musica.loop(1.0f,0.2f);
    }

    @Override
    public void render(GameContainer container, StateBasedGame game, Graphics g) throws SlickException {
        escenas.get(j.getEscenario()).getMapa_escena().render(0,0);  
        j.getHud().imprimirRestosEnemigos();
        j.getVarita().getFlecha().draw(g,menu);
        
        //Cofres
        for(int i = 0; i < escenas.get(j.getEscenario()).getCofres().size(); i++){
            escenas.get(j.getEscenario()).getCofres().get(i).imprimir_cofre();
            escenas.get(j.getEscenario()).getCofres().get(i).imprimirTeclaAbrirCofre();
        }
        
        //Jugador
        j.imprimirJugador();
        
        //Imprimir enemigos
        for(int i = 0;i<escenas.get(j.getEscenario()).getEnemigos().size();i++){
            escenas.get(j.getEscenario()).getEnemigos().get(i).imprimirEnemigo();
        } 
        
        //Ocultamos las habitaciones donde no está el protagonista
        this.ocultarArea();
        
        //HUD e inventario
        j.getInventario().imprimirImagenInfoPociones();   
        j.getHud().imprimirCorazones();
        j.getInventario().imprimirInventario();
        j.getHud().imprime_estados( j.getBuffFuerza(), j.getBuffInv(), j.getBuffVelo());
        j.getHud().imprimirHudExpMunicion(g,j.getVarita().getMunicion() , j.getExperiencia());
        j.getNoti().imprimirNotificaciones();
        menu.mostrarMenu(g,j,escenas);
    }
    
    @Override
    public void update(GameContainer container, StateBasedGame game, int delta) throws SlickException {
        reloj+= delta;
        menu.gestionarMenuPausa(entrada, container, game,musica);
        this.comprobarPos();
        if(!menu.isPausa()){
            j.gestionarJugador(container, game, numEscenas, delta, entrada, datos, escenas,almacenar,musica);
        } 
        for(int i = 0;i<escenas.get(j.getEscenario()).getEnemigos().size();i++){
            escenas.get(j.getEscenario()).getEnemigos().get(i).realizarMovimiento(j, escenas.get(j.getEscenario()), delta, reloj);
        }
        if(reloj > 2000){
            reloj = 0;
        }
    }
    
    @Override
    public void enter(GameContainer container,StateBasedGame game)throws SlickException{
        container.getInput().clearKeyPressedRecord();
        init(container, game);     
    }
   
    public void comprobarPos(){
        if((j.getPunto().getY()+j.getPersL().getHeight()) <= 192){
            this.area1 = false;
            this.area2 = true;
            this.area3 = true;
        }else if((j.getPunto().getY()+j.getPersL().getHeight()) >= 448){
            this.area1 = true;
            this.area2 = true;
            this.area3= false;
        }else{
            this.area1 = true;
            this.area2 = false;
            this.area3 = true;
        }
    }
    
    public void ocultarArea(){
        if(this.area1){
            area1S.draw();
        }
        if(this.area2){
            area2S.draw();
        }
        if(this.area3){
            area3S.draw();
        }
    }
    
    @Override
    public void mouseClicked(int button, int x, int y, int clickCount){
            System.out.println("Eje x: " +x + " Eje y: " + y);
    }
       
    @Override
    public int getID() {
        return 2;
    }
    
}
