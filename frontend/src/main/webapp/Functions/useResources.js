import  {useState, useEffect } from 'react';
//import axios from 'axios';

const useResources = resource => {
    const [resources, setResources] = useState([]);
    
    useEffect(() => {
        async function getCharacters() {
          try {
            const response = await fetch(resource);
            const body = await response.json();
            //setResources(body);
            setResources(body.map(({ name, id }) => ({ label: name, value: id })));
          } catch (error) {
            console.log(error);
          }
        }
        getCharacters();
      }, []);
      return resources;
}

export default useResources;