import React from 'react'
import ReactDOM from 'react-dom/client'
import App from "@/App"
import DataContextProvider from "@/state/DataContextProvider";
import {FormStateComponent} from "@/state/FormState";

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
      <FormStateComponent>
        <App />
      </FormStateComponent>
  </React.StrictMode>
)
