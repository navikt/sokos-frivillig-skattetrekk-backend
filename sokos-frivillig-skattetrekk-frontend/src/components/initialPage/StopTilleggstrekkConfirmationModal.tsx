import {useContext, useState} from 'react'
import {BodyLong, Button, HStack, Modal} from '@navikt/ds-react'
import {SatsType, saveSkattetrekk} from "@/api/skattetrekkBackendClient";
import {DataContext} from "@/state/DataContextProvider";

export function StopTilleggstrekkConfirmationModal() {
    const [open, setOpen] = useState(false)
    const [isLoading, setIsLoading] = useState(false)
    const {setShouldRefetch} = useContext(DataContext)

    async function onConfirm() {
        setIsLoading(true)
        saveSkattetrekk(
            {
                value: 0,
                satsType: SatsType.KRONER,
            })
        setShouldRefetch(true)
        setIsLoading(false)
        setOpen(false)
    }


    return (
        <>
            <HStack gap="6">
                <Button type="button" onClick={() => setOpen(true)} variant="secondary">
                    Stopp frivillig skattetrekk
                </Button>
            </HStack>

            <Modal open={open} onClose={() => setOpen(false)} header={{ heading: 'Stoppe frivillig skattetrekk' }} closeOnBackdropClick width="medium">
                <Modal.Body>
                    <BodyLong>Hvis du velger å stoppe det frivillige skattetrekket ditt, blir det stoppet fra og med neste måned. Vil du stoppe det frivillige skattetrekket ditt?</BodyLong>
                </Modal.Body>
                <Modal.Footer>

                    <Button type="button" onClick={onConfirm} variant="primary" loading={isLoading}>
                        Ja
                    </Button>
                    <Button type="button" variant="secondary" onClick={() => {setOpen(false)}}>
                        Nei
                    </Button>
                </Modal.Footer>
            </Modal>
        </>
    )
}
