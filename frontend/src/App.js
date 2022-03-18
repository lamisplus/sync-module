import React from "react";
import {
  BrowserRouter as Router,
  Switch,
  Route,
  Link
} from "react-router-dom";
import SyncList from './main/webapp/components/Sync/SyncList';

import HomePage from './main/webapp/components/Home'
import SettingPage from './main/webapp/components/Settings'
import Dashboard from './main/webapp/components/Dashboard'
import TabMenu from './main/webapp/components/TabMenu'
import { ToastContainer, toast } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';

export default function App() {
  return (
    <Router>
      <div>
      <ToastContainer />
        {/* A <Switch> looks through its children <Route>s and
            renders the first one that matches the current URL. */}
        <Switch>
          <Route path="/sync">
            <TabMenuPage />
          </Route>
          <Route path="/settings">
            <SettingPage />
          </Route>
          <Route path="/">
            <Dashboard />
          </Route>
          <Route path="/tab-menu">
            <TabMenuPage />
          </Route>
        </Switch>
      </div>
    </Router>
  );
}

function Home() {
  return <Dashboard />;
}
function SyncListPage() {
  return <SyncList />;
}

function TabMenuPage() {
  return <TabMenu />;
}

