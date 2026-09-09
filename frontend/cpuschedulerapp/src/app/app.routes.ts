import { Routes } from '@angular/router';

import {Home} from './home/home';
import {Scenario} from './scenario/scenario';
import { CreateScenario } from './create-scenario/create-scenario';
import { GenerateResourceRequests } from './generate-resource-requests/generate-resource-requests';
import { Simulation } from './simulation/simulation'
import { unsavedChangesGuard } from './guards/unsaved-changes-guard';

export const routes: Routes = [
    {path: 'scenario',component: Scenario},
    {path: '',component: Home},
    {path: 'createScenario', component:CreateScenario, canDeactivate: [unsavedChangesGuard]},
    {path: 'GenerateResourceRequests/:id', component:GenerateResourceRequests},
    {path: 'simulation/scenario/:id', component:Simulation}
];
