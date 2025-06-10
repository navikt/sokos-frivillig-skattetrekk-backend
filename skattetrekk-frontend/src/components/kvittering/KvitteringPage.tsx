import {Alert, BodyLong, Heading, Link, List, VStack} from '@navikt/ds-react'
import React, {useContext, useEffect} from 'react'
import {numberFormatWithKr} from "@/common/Utils";
import {MessageType, SatsType} from "@/api/skattetrekkBackendClient";
import {DataContext} from "@/state/DataContextProvider";
import {PageLinks} from "@/routes";
import {useLocation, useNavigate} from "react-router-dom";

export const KvitteringPage = () => {
    const {sendResponse} = useContext(DataContext)
    const location = useLocation()
    const navigate = useNavigate()
    const pid: string = window.history.state.pid;


    useEffect(() => {
    if(sendResponse === null) {
        navigate(import.meta.env.BASE_URL + PageLinks.INDEX, {state: {pid: pid}})
    }}, [sendResponse])

    if (sendResponse === null) {
        return null
    }


    if (sendResponse?.messages?.some((msg: { type: MessageType }) => msg.type === MessageType.ERROR)) {
        return (
            <VStack gap="6" className="form-container">
                <Alert variant="error">
                    <VStack gap="3">
                        Det har skjedd en teknisk feil. Hvis du har registrert informasjon, har den dessverre ikke blitt lagret. Vi beklager for dette. Du kan prøve igjen senere.
                        Ta gjerne kontakt med oss hvis problemet fortsetter.
                    </VStack>
                </Alert>
            </VStack>
        )
    }

  return (
      <VStack gap="6" className="form-container">
          <Alert variant="success">
              <VStack gap="3">
                  <Heading level="3" size="small">
                      {sendResponse.data.framtidigTilleggstrekk?.satsType === SatsType.PROSENT ?
                          `Frivillig skattetrekk på ${sendResponse.data.framtidigTilleggstrekk?.sats} % registrert` :
                          `Frivillig skattetrekk på ${numberFormatWithKr(sendResponse.data.framtidigTilleggstrekk?.sats ?? 0)} per måned registrert`}
                  </Heading>
                  <BodyLong>
                      Skattetrekket gjelder ut året.
                  </BodyLong>
              </VStack>
          </Alert>


          <List>
              <List.Item>Frivillig skattetrekk stoppes automatisk ved årsskiftet, du må derfor legge inn et nytt trekk
                  for hvert hvert år.</List.Item>
              <List.Item>Hvis det er mindre enn 14 dager før neste utbetaling, kan det være at trekket ikke kommer med
                  før neste gang..</List.Item>
          </List>

          <div style={{borderBottom: '0.5px solid black', width: '100%'}}/>

          <Link href="https://www.nav.no/minside" target="_blank">Gå til Min side</Link>
          <Link onClick={() => navigate(import.meta.env.BASE_URL + PageLinks.INDEX, {state: {pid: location.state.pid}})}>
              Endre registrert frivillig skattetrekk</Link>
      </VStack>
  )
}
