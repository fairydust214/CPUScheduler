import { Routes } from '@angular/router';

import {Home} from './home/home';
import {Scenario} from './scenario/scenario';
import { CreateScenario } from './create-scenario/create-scenario';
import { GenerateResourceRequests } from './generate-resource-requests/generate-resource-requests';

export const routes: Routes = [
    {path: 'scenario',component: Scenario},
    {path: '',component: Home},
    {path: 'createScenario', component:CreateScenario},
    {path: 'GenerateResourceRequests/:id', component:GenerateResourceRequests}
];
