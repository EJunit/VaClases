package hn.uth.hackaton.Validacion;

public class Validacion {

    private String fecha_ini;
    private String fecha_fin;
    private String cant_dias;
    private int type;
    private String fecha_problema;
    private String id_validacion_clases;
    private String id_problema;
    private String preguntas;

    //clases
    public Validacion(String f1, String fecha_fin, String dias, int type, String id_validacion_clases) {
        this.fecha_ini = f1;
        this.fecha_fin = fecha_fin;
        this.cant_dias = dias;
        this.id_validacion_clases = id_validacion_clases;
        this.type = type;
    }

    //problemas
    public Validacion(int type, String fecha, String id_problema, String preguntas) {
        this.fecha_problema = fecha;
        this.id_problema = id_problema;
        this.type = type;
        this.preguntas = preguntas;
    }

    public String getFecha_ini() {
        return fecha_ini;
    }

    public String getCant_dias() {
        return cant_dias;
    }

    public int getType() {
        return type;
    }

    public String getFecha_problema() {
        return fecha_problema;
    }

    public String getId_validacion_clases() {
        return id_validacion_clases;
    }

    public String getId_problema() {
        return id_problema;
    }

    public String getPreguntas() {
        return preguntas;
    }

    public String getFecha_fin() {
        return fecha_fin;
    }
}
