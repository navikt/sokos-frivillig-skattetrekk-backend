import {Alert, BodyLong, BodyShort, Box, Heading, Link, List, Loader, VStack} from '@navikt/ds-react'
import React, {useContext, useState} from 'react'
import {numberFormatWithKr} from "@/common/Utils";
import {MessageType, SatsType} from "@/api/skattetrekkBackendClient";
import {DataContext} from "@/state/DataContextProvider";
import {PageLinks} from "@/routes";
import {useNavigate} from "react-router-dom";

export const KvitteringPage = (props: {
}) => {
    const {sendResponse} = useContext(DataContext)
    const [isWaiting, setIsWaiting] = useState(true)

    const navigate = useNavigate()
    const pid = new URLSearchParams(document.location.search).get("pid")

    console.log("sendResponse", sendResponse)

    if(sendResponse === null) {
        return (
            <Box background="bg-subtle" padding="16" borderRadius="large">
                <VStack align="center" gap="8">
                    <Heading align="center" size={"large"} level="2">
                        Vent mens vi sender inn
                    </Heading>
                    <Loader size="3xlarge" />
                    <BodyShort align="center">Dette kan ta opptil ett minutt.</BodyShort>
                </VStack>
            </Box>
        )
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
                      {/*TODO PEB-1184 review logikken*/}
                      {sendResponse.data.framtidigTilleggstrekk?.satsType === SatsType.PROSENT ?
                          `Frivillig skattetrekk på ${sendResponse.data.tilleggstrekk?.sats} % registrert` :
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
          <Link onClick={() => navigate(import.meta.env.BASE_URL + PageLinks.INDEX, {state: {pid: pid}})}
                target="_blank">Endre registrert frivillig skattetrekk</Link>
      </VStack>
  )
}
