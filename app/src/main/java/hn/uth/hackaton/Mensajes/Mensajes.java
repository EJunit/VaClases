package hn.uth.hackaton.Mensajes;

/**
 * Modelo de datos estático para alimentar la aplicación
 */
public class Mensajes {

    private String titulo;
    private String mensaje;
    private String fecha;
    private String send_type;

    public Mensajes() {
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getSend_type() {
        return send_type;
    }

    public void setSend_type(String send_type) {
        this.send_type = send_type;
    }
}
