import React from 'react'
import ReactDOM from 'react-dom/client'
import App from "@/App"
import DataContextProvider from "@/state/DataContextProvider";
import "@navikt/ds-css";
import "./App.css";

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>

          <div className="microfrontend-container">
              <representasjon-banner
                  representasjonstyper="PENSJON_FULLSTENDIG,PENSJON_BEGRENSET,PENSJON_SKRIV,PENSJON_KOMMUNISER,PENSJON_LES,PENSJON_SAMHANDLER,PENSJON_SAMHANDLER_ADMIN,PENSJON_SUPERADMIN"
                  redirectTo={`${window.location.origin}/utbetaling/skattetrekk/`}
              ></representasjon-banner>
          </div>
        <App />
  </React.StrictMode>
)
