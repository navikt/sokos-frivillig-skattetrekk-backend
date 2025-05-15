import {Alert, BodyShort, Heading, Link, List, VStack} from '@navikt/ds-react'
import React, {useContext, useEffect, useState} from 'react'
import {numberFormatWithKr} from "@/common/Utils";
import {SatsType} from "@/api/skattetrekkBackendClient";
import {FormStateContext} from "@/state/FormState";
import {RegistrerteSkattetrekk} from "@/components/initialPage/RegistrerteSkattetrekk";
import {DataContext} from "@/state/DataContextProvider";

export const KvitteringPage = (props: {
}) => {
  const {tilleggstrekkType, tilleggstrekkValue} = useContext(FormStateContext)
  const {sendResponse} = useContext(DataContext)
  const [isWaiting, setIsWaiting] = useState(true)

  return (
    <VStack gap="6" className="form-container">
      <Alert variant="success">
        <VStack gap="3">
          <Heading level="3" size="small">
            {tilleggstrekkType === SatsType.PROSENT ?
                `Frivillig skattetrekk på ${tilleggstrekkValue} % registrert` :
                `Frivillig skattetrekk på ${numberFormatWithKr(tilleggstrekkValue ?? 0)} per måned registrert`}
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

        { sendResponse != null &&
            <VStack gap={{xs: "2", md: "6"}}>
                <Heading size={"medium"} level="2">Dine registrerte skattetrekk</Heading>
                <RegistrerteSkattetrekk skatteTrekk={sendResponse!.data?.skattetrekk!} tilleggstrekk={sendResponse.data!.tilleggstrekk!} framtidigTilleggstrekk={sendResponse!.data!.framtidigTilleggstrekk} />
            </VStack> }

      <Link href="https://www.nav.no/skattetrekk" target="_blank">Endre registrert frivillig skattetrekk</Link>
    </VStack>
  )
}
