import { render } from '@testing-library/react'
import { Loadable } from './utils'

describe('utils', () => {
    it('should match snapshot', async () => {
        const result = render(
            <Loadable><div>foo</div></Loadable>
        )
        expect(result.container).toMatchSnapshot()
    })
    it('should match loading snapshot', async () => {
        const result = render(
            <Loadable isLoading />
        )
        expect(result.container).toMatchSnapshot()
    })

})
