package ds.vkplus.exception

import ds.vkplus.model.ApiResponse

class VKException : RuntimeException {

    var code: Int = 0
    var mess: String


    constructor(e: ApiResponse.Error) {
        code = e.error_code
        mess = e.error_msg
    }


    constructor() {
        code = CODE_UNKNOWN
        mess = "Unknown Error"

    }

    companion object {
        const val CODE_UNKNOWN: Int = -1
        const val CODE_NETWORK: Int = -2
        const val CODE_TOKEN_OBSOLETED: Int = 5
    }
}
