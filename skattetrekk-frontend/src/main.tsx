import React from 'react'
import ReactDOM from 'react-dom/client'
import App from "@/App"
import "@navikt/ds-css";
import "./App.css";

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
          <div className="microfrontend-container">
              <representasjon-banner
                  representasjonstyper=""
                  redirectTo={`${window.location.origin}/utbetaling/skattetrekk/`}
              ></representasjon-banner>
          </div>
        <App />
  </React.StrictMode>
)
