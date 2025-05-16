import {Alert, BodyShort, Heading, Link, List, VStack} from '@navikt/ds-react'
import React, {useContext, useEffect, useState} from 'react'
import {numberFormatWithKr} from "@/common/Utils";
import {MessageType, SatsType} from "@/api/skattetrekkBackendClient";
import {FormStateContext} from "@/state/FormState";
import {RegistrerteSkattetrekk} from "@/components/initialPage/RegistrerteSkattetrekk";
import {DataContext} from "@/state/DataContextProvider";

export const KvitteringPage = (props: {
}) => {
    const {sendResponse} = useContext(DataContext)
    const [isWaiting, setIsWaiting] = useState(true)

    if (sendResponse === null || sendResponse.messages?.some((msg: { type: MessageType }) => msg.type === MessageType.ERROR)) {
        return (
            <VStack gap="6" className="form-container">
                <Alert variant="error">
                    <VStack gap="3">
                        <Heading level="3" size="small">
                            Det har skjedd en teknisk feil. Hvis du har registrert informasjon, har den dessverre ikke blitt lagret. Vi beklager for dette. Du kan prøve igjen senere.
                            Ta gjerne kontakt med oss hvis problemet fortsetter.
                        </Heading>
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
            {sendResponse.data.tilleggstrekk?.satsType === SatsType.PROSENT ?
                `Frivillig skattetrekk på ${sendResponse.data.tilleggstrekk?.sats} % registrert` :
                `Frivillig skattetrekk på ${numberFormatWithKr(sendResponse.data.framtidigTilleggstrekk?.sats ?? 0)} per måned registrert`}
          </Heading>
          <BodyShort>
            Skattetrekket gjelder fra og med neste måned og ut året.
          </BodyShort>
        </VStack>
      </Alert>

      <List>
        <List.Item>Frivillig skattetrekk stoppes automatisk ved årsskiftet,  du må derfor legge inn et nytt trekk for hvert hvert år.</List.Item>
        <List.Item>Hvis du har lagt inn frivillig skattetrekk i slutten av måneden, kan det gå én måned ekstra før det starter å løpe.</List.Item>
      </List>

      {sendResponse?.data &&
        <VStack gap={{xs: "2", md: "6"}}>
            <Heading size={"medium"} level="2">Dine registrerte skattetrekk</Heading>
            <RegistrerteSkattetrekk skatteTrekk={sendResponse.data!.skattetrekk!} tilleggstrekk={sendResponse.data!.tilleggstrekk} framtidigTilleggstrekk={sendResponse.data!.framtidigTilleggstrekk} />
        </VStack> }

      <Link href="https://www.nav.no/skattetrekk" target="_blank">Endre registrert frivillig skattetrekk</Link>
    </VStack>
  )
}
