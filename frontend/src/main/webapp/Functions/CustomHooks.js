import React, {useState} from 'react'


const useRegForm = (callback) => {
    const [inputs, setInputs] = useState({});

    const handleSubmit = (e )=> {
        e.preventDefault();
        callback();
    }

    const handleInputChange = (e) => {
        setInputs(inputs => ({...inputs, [e.target.name]: e.target.value}))
    }

    return {
        handleSubmit,
        handleInputChange,
        inputs
    }

}

export default useRegForm