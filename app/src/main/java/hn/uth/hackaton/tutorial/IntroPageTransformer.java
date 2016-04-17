package hn.uth.hackaton.tutorial;

import android.support.v4.view.ViewPager;
import android.view.View;

import hn.uth.hackaton.R;

public class IntroPageTransformer implements ViewPager.PageTransformer {

    @Override
    public void transformPage(View page, float position) {

        // Obtener el índice de la página de la etiqueta. Esto hace
        // Es posible saber qué página de índice que eres
        // Actualmente transformando - y que puede ser utilizado
        // Hacer algunas mejoras importantes de rendimiento.
        int pagePosition = (int) page.getTag();

        // Aquí se puede hacer cualquier cosa, como obtener la
        // Ancho de la página y realizar cálculos con base
        // De lo lejos que el usuario ha movido la página.
        int pageWidth = page.getWidth();
        float pageWidthTimesPosition = pageWidth * position;
        float absPosition = Math.abs(position);

        // Ahora es el tiempo para que los efectos inicien
        if(position <= -1.0f || position >= 1.0f) {

            // La página no es visible. Este es un buen lugar para parar
            // Cualquier potencial de trabajo / animaciones es posible que se estén ejecutando.

        } else if (position == 0.0f) {

            // Se selecciona la página. Este es un buen momento para restablecer Vistas
            // Después de animaciones como no siempre se puede contar con el PageTransformer
            // Devoluciones de llamada para que coincida perfectamente.

        } else {

            //La página se está desplazando actualmente. Este es
            // Un buen lugar para mostrar animaciones que reaccionan al usuario de
            // Deslizar ya que proporciona una buena experiencia de usuario.

            // Vamos a empezar por la animación del título.
            // Queremos que se desvanecen como se desplaza hacia fuera
            View title = page.findViewById(R.id.title);
            title.setAlpha(1.0f - absPosition);

            // Ahora la descripción. También queremos que éste a
            // Se desvanecen, pero la animación también debe moverse lentamente
            // Abajo y hacia fuera de la pantalla
            View description = page.findViewById(R.id.description);
            description.setTranslationY(-pageWidthTimesPosition / 2f);
            description.setAlpha(1.0f - absPosition);

            // Ahora, queremos que la imagen se mueva hacia la derecha,
            // Es decir, en la dirección opuesta de el resto de la
            // Contenido mientras desvanecimiento
            View imgIntro1 = page.findViewById(R.id.imgIntro1);
            View imgIntro2 = page.findViewById(R.id.imgIntro2);
            View imgIntro3 = page.findViewById(R.id.imgIntro3);

            // Estamos tratando de crear un efecto de Vista
            // Específica a una de las páginas de nuestro ViewPager.
            // En otras palabras, tenemos que comprobar que estamos en
            // La página correcta y que la vista de que se trata
            // No es nulo.
            if ((pagePosition == 0 || pagePosition == 1) && imgIntro1 != null) {
                imgIntro1.setAlpha(1.0f - absPosition);
                imgIntro1.setTranslationX(-pageWidthTimesPosition * 1.5f);
            }

            if (pagePosition == 1 && imgIntro2 != null) {
                imgIntro2.setAlpha(1.0f - absPosition);
                imgIntro2.setTranslationX(-pageWidthTimesPosition * 1.5f);
            }

            if (pagePosition == 2 && imgIntro3 != null) {
                imgIntro3.setAlpha(1.0f - absPosition);
                imgIntro3.setTranslationX(-pageWidthTimesPosition * 1.5f);
            }

            // Finalmente, puede ser útil conocer la dirección
            // Del swipe del usuario - si estamos entrando o saliendo.
            // Esto es bastante simple:
            if (position < 0) {
                // Create your out animation here
            } else {
                // Create your in animation here
            }
        }
    }

}