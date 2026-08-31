import { Routes } from '@angular/router';

import {Home} from './home/home';
import {Scenario} from './scenario/scenario';

export const routes: Routes = [
    {path: 'scenario',component: Scenario},
    {path: '',component: Home}
];
