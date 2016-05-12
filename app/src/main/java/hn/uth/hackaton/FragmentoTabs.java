package hn.uth.hackaton;

import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import hn.uth.hackaton.Eventos.FragmentoEventos;
import hn.uth.hackaton.Mensajes.FragmentoMensaje;
import hn.uth.hackaton.Validacion.FragmentoValidacion;


public class FragmentoTabs extends Fragment {

    private TabLayout pestanas;

    public FragmentoTabs() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragmento_paginado, container, false);

        if (savedInstanceState == null) {
            insertarTabs(container);
            // Setear adaptador al viewpager.
            ViewPager viewPager = (ViewPager) view.findViewById(R.id.pager);
            poblarViewPager(viewPager);
            pestanas.setupWithViewPager(viewPager);

        }

        return view;
    }

    private void insertarTabs(ViewGroup container) {
        View padre = (View) container.getParent();
        AppBarLayout appBar = (AppBarLayout) padre.findViewById(R.id.appbar_inicio);
        pestanas = new TabLayout(getActivity());
        pestanas.setTabTextColors(Color.parseColor("#FFFFFF"), Color.parseColor("#FFFFFF"));
        appBar.addView(pestanas);


    }

    private void poblarViewPager(ViewPager viewPager) {
        AdaptadorSecciones adapter = new AdaptadorSecciones(getFragmentManager());
        adapter.addFragment(new FragmentoValidacion(), getString(R.string.titulo_tab_validacion));
        adapter.addFragment(new FragmentoMensaje(), getString(R.string.titulo_tab_mensaje));
        adapter.addFragment(new FragmentoEventos(), getString(R.string.titulo_tab_evento));
        viewPager.setAdapter(adapter);
        //pestanas.getTabAt(1).setCustomView(new FragmentoMensaje(), getString(R.string.titulo_tab_mensaje));
    }

    public class AdaptadorSecciones extends FragmentStatePagerAdapter {
        private final List<Fragment> fragmentos = new ArrayList<>();
        private final List<String> titulosFragmentos = new ArrayList<>();

        public AdaptadorSecciones(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return fragmentos.get(position);
        }

        @Override
        public int getCount() {
            return fragmentos.size();
        }

        public void addFragment(Fragment fragment, String title) {
            fragmentos.add(fragment);
            titulosFragmentos.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return titulosFragmentos.get(position);
        }

    }
}
