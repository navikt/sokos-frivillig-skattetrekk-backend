import {Alert, BodyShort, Heading, Link, List, VStack} from '@navikt/ds-react'
import React, {useContext, useEffect, useState} from 'react'
import {numberFormatWithKr} from "@/common/Utils";
import {SatsType} from "@/api/skattetrekkBackendClient";
import {FormStateContext} from "@/state/FormState";
import ListItem from "@navikt/ds-react/esm/dropdown/Menu/List/Item";

export const KvitteringPage = (props: {
}) => {
  const {tilleggstrekkType, tilleggstrekkValue} = useContext(FormStateContext)
  const [isWaiting, setIsWaiting] = useState(true)

    useEffect(() => {
        console.log(tilleggstrekkType, tilleggstrekkValue)
    }, []);

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
        <ListItem>Frivillig skattetrekk stoppes automatisk ved årsskiftet,  du må derfor legge inn et nytt trekk for hvert hvert år.</ListItem>
        <ListItem>Hvis du har lagt inn frivillig skattetrekk i slutten av måneden, kan det gå én måned ekstra før det starter å løpe.</ListItem>
      </List>

      <VStack gap={{xs: "2", md: "6"}}>
        <Heading size={"medium"} level="2">Dine registrerte skattetrekk</Heading>
        <BodyShort><strong>Trekk fra skattekortet:</strong></BodyShort>
        <BodyShort><strong>Frivillig skattetrekk:</strong></BodyShort>
      </VStack>

      <Link href="https://www.nav.no/skattetrekk" target="_blank">Endre registrert frivillig skattetrekk</Link>
    </VStack>
  )
}
