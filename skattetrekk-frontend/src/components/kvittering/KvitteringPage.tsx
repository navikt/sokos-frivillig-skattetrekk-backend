import {Alert, BodyLong, Heading, Link, List, VStack} from '@navikt/ds-react'
import React, {useContext, useEffect} from 'react'
import {numberFormatWithKr} from "@/common/Utils";
import {FrivilligSkattetrekkData, MessageType, SatsType} from "@/api/skattetrekkBackendClient";
import {DataContext} from "@/state/DataContextProvider";
import {PageLinks} from "@/routes";
import {useLocationState} from "@/common/useLocationState";

export const KvitteringPage = () => {
    const { setShouldRefetch, getResponse } = useContext(DataContext)
    const { pid, navigate, isSent } = useLocationState()

    useEffect(() => {
        if(!isSent) {
            navigate(PageLinks.INDEX)
        }

        setShouldRefetch(true)
    }, [isSent])

    if (getResponse === null) {
        return null
    }

    function visRiktigNyregistertTilleggstrekk(data: FrivilligSkattetrekkData) {

        var registrertFrivilligSkattetrekk = !data.fremtidigTilleggstrekk ? data.tilleggstrekk : data.fremtidigTilleggstrekk

        return(<>
            {registrertFrivilligSkattetrekk?.satsType === SatsType.PROSENT ?
                    `Frivillig skattetrekk på ${registrertFrivilligSkattetrekk?.sats} % registrert` :
                    `Frivillig skattetrekk på ${numberFormatWithKr(registrertFrivilligSkattetrekk?.sats ?? 0)} per måned registrert`}
        </>)
    }

    if (getResponse.messages?.some((msg: { type: MessageType }) => msg.type === MessageType.ERROR)) {
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
                      {visRiktigNyregistertTilleggstrekk(getResponse.data)}
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
                  før neste gang.</List.Item>
          </List>

          <div style={{borderBottom: '0.5px solid black', width: '100%'}}/>

          <Link href="https://www.nav.no/minside" target="_blank">Gå til Min side</Link>
          <Link onClick={() => navigate(PageLinks.INDEX,  { pid })}>
              Endre registrert frivillig skattetrekk</Link>
      </VStack>
  )
}
