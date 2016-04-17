package hn.uth.hackaton.tutorial;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import hn.uth.hackaton.Login.LoginActivity;
import hn.uth.hackaton.Preferencias;
import hn.uth.hackaton.R;

public class IntroFragment extends Fragment {

    private static final String BACKGROUND_COLOR = "backgroundColor";
    private static final String PAGE = "page";

    private int mBackgroundColor, mPage;

    public static IntroFragment newInstance(int backgroundColor, int page) {
        IntroFragment frag = new IntroFragment();
        Bundle b = new Bundle();
        b.putInt(BACKGROUND_COLOR, backgroundColor);
        b.putInt(PAGE, page);
        frag.setArguments(b);
        return frag;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!getArguments().containsKey(BACKGROUND_COLOR))
            throw new RuntimeException("Fragment must contain a \"" + BACKGROUND_COLOR + "\" argument!");
        mBackgroundColor = getArguments().getInt(BACKGROUND_COLOR);

        if (!getArguments().containsKey(PAGE))
            throw new RuntimeException("Fragment must contain a \"" + PAGE + "\" argument!");
        mPage = getArguments().getInt(PAGE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Seleccione un diseño basado en la página actual
        int layoutResId;
        switch (mPage) {
            case 0:
                layoutResId = R.layout.intro_fragment_layout_1;
                break;
            case 1:
                layoutResId = R.layout.intro_fragment_layout_2;
                break;
            case 2:
                layoutResId = R.layout.intro_fragment_layout_3;
                break;
            default:
                layoutResId = R.layout.intro_fragment_layout_4;

        }

        // Inflate the layout resource file

        Typeface RobotoSlab_bold = Typeface.createFromAsset(getContext().getAssets(), "fonts/RobotoSlab-Bold.ttf");
        Typeface roboto_light = Typeface.createFromAsset(getContext().getAssets(), "fonts/Roboto-Light.ttf");

        View view = getActivity().getLayoutInflater().inflate(layoutResId, container, false);

        TextView titulo = (TextView) view.findViewById(R.id.title);
        TextView descripcion = (TextView) view.findViewById(R.id.description);

        titulo.setTypeface(RobotoSlab_bold);
        descripcion.setTypeface(roboto_light);

        //agregamos la funcionalidad a los botones
        try{
            //boton DONE
            Button done = (Button) view.findViewById(R.id.btnDone);
            done.setTypeface(roboto_light);

            done.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openLogin();
                }
            });
        }catch (Exception ignored){

        }

        // Set the current page index as the View's tag (useful in the PageTransformer)
        view.setTag(mPage);

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Set the background color of the root view to the color specified in newInstance()
        View background = view.findViewById(R.id.intro_background);
        background.setBackgroundColor(mBackgroundColor);
    }

    private void openLogin() {

        Preferencias p = new Preferencias(getContext());

        p.setFlagTuto("1");

        Intent intent = new Intent(getContext(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        getActivity().finish();
    }

}